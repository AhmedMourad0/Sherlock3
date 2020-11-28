package dev.ahmedmourad.sherlock.auth

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import arrow.core.Either
import arrow.core.left
import arrow.core.orNull
import arrow.core.right
import dev.ahmedmourad.sherlock.auth.fakes.FakeAuthenticator
import dev.ahmedmourad.sherlock.auth.fakes.FakeImageRepository
import dev.ahmedmourad.sherlock.auth.fakes.FakeRemoteRepository
import dev.ahmedmourad.sherlock.auth.manager.AuthManagerImpl
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.auth.utils.*
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.*
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class AuthManagerImplTests {

    private lateinit var authenticator: FakeAuthenticator
    private lateinit var remoteRepository: FakeRemoteRepository
    private lateinit var imageRepository: FakeImageRepository

    private lateinit var manager: AuthManagerImpl

    @Before
    fun setup() {

        remoteRepository = FakeRemoteRepository()
        imageRepository = FakeImageRepository()
        authenticator = FakeAuthenticator(::incompleteUser)

        manager = AuthManagerImpl({ authenticator }, { remoteRepository }, { imageRepository })
    }

    @Test
    fun observeUserAuthState_shouldReturnTrueIfTheUserIsSignedInOrFalseOtherwise() {

        manager.observeUserAuthState()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(false.right())

        val credentials = userCredentials()

        authenticator.signUp(credentials).test().await().assertNoErrors().assertComplete()

        manager.observeUserAuthState()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(true.right())

        authenticator.signOut().test().await().assertNoErrors().assertComplete()

        manager.observeUserAuthState()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(false.right())
    }

    @Test
    fun observeUserAuthState_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            manager.observeUserAuthState().firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == AuthManager.ObserveUserAuthStateException.NoInternetConnectionException
            }
        }

        authenticator.observeUserAuthStateException =
                Authenticator.ObserveUserAuthStateException.NoInternetConnectionException
        go()
    }

    @Test
    fun observeUserAuthState_shouldPropagateTheUnknownException() {

        fun go() {
            manager.observeUserAuthState().firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.ObserveUserAuthStateException.UnknownException
            }
        }

        authenticator.observeUserAuthStateException =
                Authenticator.ObserveUserAuthStateException.UnknownException(RuntimeException())
        go()
    }

    @Test
    fun observeSignedInUser_shouldReturnNullIfThereIsNoCurrentSignedInUser() {
        manager.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue(null.right())
    }

    @Test
    fun observeSignedInUser_shouldReturnIncompleteUserIfTheCurrentUserHasNotCompletedTheRegistrationProcess() {

        val credentials = userCredentials()

        authenticator.signUp(credentials).test().await().assertNoErrors().assertComplete()

        manager.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    val u = either.b
                    assertNotNull(u)
                    assertTrue(u!!.isLeft())
                    u as Either.Left
                    u.a.matches(credentials)
                }
    }

    @Test
    fun observeSignedInUser_shouldReturnSignedInUserIfTheCurrentUserHasCompletedTheRegistrationProcess() {

        val credentials = userCredentials()
        val incomplete = authenticator.signUp(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .values()[0]
        assertTrue(incomplete.isRight())
        incomplete as Either.Right

        val user = remoteSignUpUser(incomplete.b.id, credentials.email)
        remoteRepository.storeSignUpUser(user)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        manager.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    val u = either.b
                    assertNotNull(u)
                    assertTrue(u!!.isRight())
                    u as Either.Right
                    u.b.matches(credentials, user)
                }
    }

    @Test
    fun observeSignedInUser_shouldPropagateTheNoInternetConnectionException() {

        authenticator.signUp(userCredentials())
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        fun go() {
            manager.observeSignedInUser().firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == AuthManager.ObserveSignedInUserException.NoInternetConnectionException
            }
        }

        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.NoInternetConnectionException
        remoteRepository.hasInternet = true
        go()

        authenticator.observeSignedInUserException = null
        remoteRepository.hasInternet = false
        go()

        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.NoInternetConnectionException
        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun observeSignedInUser_shouldPropagateTheInternalException() {

        authenticator.signUp(userCredentials())
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        fun go() {
            manager.observeSignedInUser().firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.ObserveSignedInUserException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        go()
    }

    @Test
    fun observeSignedInUser_shouldPropagateTheUnknownException() {

        fun go() {
            manager.observeSignedInUser().firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.ObserveSignedInUserException.UnknownException
            }
        }

        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.UnknownException(RuntimeException())
        remoteRepository.triggerUnknownException = false
        go()

        authenticator.observeUserAuthStateException = null
        remoteRepository.triggerUnknownException = true
        go()

        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.UnknownException(RuntimeException())
        remoteRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun findSimpleUsers_shouldReturnTheSimpleUsersCorrespondingToThePassedIds() {

        manager.findSimpleUsers(List((0..5).random()) { UserId(UUID.randomUUID().toString()) })
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.isEmpty()
                }

        val users = List((7..16).random()) {
            remoteSignUpUser(
                    UserId(UUID.randomUUID().toString()),
                    randomEmail()
            )
        }

        users.forEach {
            remoteRepository.storeSignUpUser(it).test().await().assertNoErrors().assertComplete()
        }

        manager.findSimpleUsers(users.shuffled().take(users.size - (2..5).random()).map { it.id })
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.all { retrieved -> users.any { it.matches(retrieved) } }
                }
    }

    @Test
    fun findSimpleUsers_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            val ids = List((0..5).random()) { UserId(UUID.randomUUID().toString()) }
            manager.findSimpleUsers(ids).firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.FindSimpleUsersException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun findSimpleUsers_shouldPropagateTheNoSignedInUserException() {

        fun go() {
            val ids = List((0..5).random()) { UserId(UUID.randomUUID().toString()) }
            manager.findSimpleUsers(ids).firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.FindSimpleUsersException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun findSimpleUsers_shouldPropagateTheUnknownException() {

        fun go() {
            val ids = List((0..5).random()) { UserId(UUID.randomUUID().toString()) }
            manager.findSimpleUsers(ids).firstOrError().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.FindSimpleUsersException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun signIn_shouldSignTheUserOfTheGivenCredentialsInAndReturnEitherIncompleteUserOrSignedInUser() {

        val credentials = userCredentials()

        val incomplete = authenticator.signUp(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .values()[0]
        assertTrue(incomplete.isRight())
        incomplete as Either.Right
        authenticator.signOut().test().await().assertNoErrors().assertComplete()

        manager.signIn(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(incomplete.b.left().right())

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.b.right())

        authenticator.signOut().test().await().assertNoErrors().assertComplete()

        val remoteSignUpUser = remoteSignUpUser(incomplete.b.id, incomplete.b.email!!)
        remoteRepository.storeSignUpUser(remoteSignUpUser)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        manager.signIn(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    val u = either.b
                    assertTrue(u.isRight())
                    u as Either.Right
                    u.b.matches(credentials, remoteSignUpUser)
                }
    }

    @Test
    fun signIn_shouldPropagateTheAccountDoesNotExistOrHasBeenDisabledException() {

        fun go() {

            val credentials = userCredentials()

            manager.signIn(credentials).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInException.AccountDoesNotExistOrHasBeenDisabledException
            }
        }

        authenticator.signInException = Authenticator.SignInException.AccountDoesNotExistOrHasBeenDisabledException
        go()
    }

    @Test
    fun signIn_shouldPropagateTheWrongPasswordException() {

        fun go() {

            val credentials = userCredentials()

            manager.signIn(credentials).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInException.WrongPasswordException
            }
        }

        authenticator.signInException = Authenticator.SignInException.WrongPasswordException
        go()
    }

    @Test
    fun signIn_shouldPropagateTheNoInternetConnectionException() {

        fun go() {

            val credentials = userCredentials()

            authenticator.signUp(credentials)
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()

            manager.signIn(credentials).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInException.NoInternetConnectionException
            }
        }

        authenticator.signInException = Authenticator.SignInException.NoInternetConnectionException
        remoteRepository.hasInternet = true
        go()

        authenticator.signInException = null
        remoteRepository.hasInternet = false
        go()

        authenticator.signInException = Authenticator.SignInException.NoInternetConnectionException
        remoteRepository.hasInternet = false
        go()
    }

    @Test
    fun signIn_shouldPropagateTheInternalException() {

        fun go() {

            val credentials = userCredentials()

            authenticator.signUp(credentials)
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()

            manager.signIn(credentials).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        go()
    }

    @Test
    fun signIn_shouldPropagateTheUnknownException() {

        fun go() {

            val credentials = userCredentials()

            authenticator.signUp(credentials)
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()

            manager.signIn(credentials).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInException.UnknownException
            }
        }

        authenticator.signInException = Authenticator.SignInException.UnknownException(RuntimeException())
        remoteRepository.triggerUnknownException = false
        go()

        authenticator.signInException = null
        remoteRepository.triggerUnknownException = true
        go()

        authenticator.signInException = Authenticator.SignInException.UnknownException(RuntimeException())
        remoteRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun signUp_shouldSignTheUserUpAndIn() {

        val user = signUpUser()

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())

        authenticator.signIn(user.credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(Authenticator.SignInException.AccountDoesNotExistOrHasBeenDisabledException.left())

        val signedInUser = manager.signUp(user)
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.matches(user)
                }.values()[0].orNull()!!

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(user.credentials.toIncompleteUser(signedInUser.id).right())

        authenticator.signOut().test().await().assertNoErrors().assertComplete()

        authenticator.signIn(user.credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(user.credentials.toIncompleteUser(signedInUser.id).right())
    }

    @Test
    fun signUp_shouldPropagateTheWeakPasswordException() {

        fun go() {

            val user = signUpUser()

            manager.signUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignUpException.WeakPasswordException
            }
        }

        authenticator.signUpException = Authenticator.SignUpException.WeakPasswordException
        go()
    }

    @Test
    fun signUp_shouldPropagateTheMalformedEmailException() {

        fun go() {

            val user = signUpUser()

            manager.signUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignUpException.MalformedEmailException
            }
        }

        authenticator.signUpException = Authenticator.SignUpException.MalformedEmailException
        go()
    }

    @Test
    fun signUp_shouldPropagateTheEmailAlreadyInUseException() {

        val user = signUpUser()

        fun go() {
            manager.signUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignUpException.EmailAlreadyInUseException
            }
        }

        authenticator.signUpException =
                Authenticator.SignUpException.EmailAlreadyInUseException(user.credentials.email)
        go()
    }

    @Test
    fun signUp_shouldPropagateTheNoInternetConnectionException() {

        fun go() {

            val user = signUpUser()

            manager.signUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignUpException.NoInternetConnectionException
            }
        }

        authenticator.signUpException = Authenticator.SignUpException.NoInternetConnectionException
        remoteRepository.hasInternet = true
        imageRepository.hasInternet = true
        go()

        authenticator.signUpException = null
        remoteRepository.hasInternet = false
        imageRepository.hasInternet = true
        go()

        authenticator.signUpException = null
        remoteRepository.hasInternet = true
        imageRepository.hasInternet = false
        go()

        authenticator.signUpException = Authenticator.SignUpException.NoInternetConnectionException
        remoteRepository.hasInternet = false
        imageRepository.hasInternet = false
        go()
    }

    @Test
    fun signUp_shouldPropagateTheInternalException() {

        fun go() {

            val user = signUpUser()

            manager.signUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignUpException.InternalException
            }
        }

        imageRepository.triggerInternalException = true
        go()
    }

    @Test
    fun signUp_shouldPropagateTheUnknownException() {

        fun go() {

            val user = signUpUser()

            manager.signUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignUpException.UnknownException
            }
        }

        authenticator.signUpException = Authenticator.SignUpException.UnknownException(RuntimeException())
        remoteRepository.triggerUnknownException = false
        imageRepository.triggerUnknownException = false
        go()

        authenticator.signUpException = null
        remoteRepository.triggerUnknownException = true
        imageRepository.triggerUnknownException = false
        go()

        authenticator.signUpException = null
        remoteRepository.triggerUnknownException = false
        imageRepository.triggerUnknownException = true
        go()

        authenticator.signUpException = Authenticator.SignUpException.UnknownException(RuntimeException())
        remoteRepository.triggerUnknownException = true
        imageRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun completeSignUp_shouldAddTheRemainingDataOfTheUserToTheRemoteRepository() {

        val credentials = userCredentials()

        val signedInUser = authenticator.signUp(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.matches(credentials)
                }.values()[0].orNull()!!

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(credentials.toIncompleteUser(signedInUser.id).right())

        remoteRepository.findSignedInUser(signedInUser.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())

        val user = completedUser(signedInUser.id, credentials.email)

        manager.completeSignUp(user)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.matches(credentials, user)
                }

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(credentials.toIncompleteUser(signedInUser.id).right())

        remoteRepository.findSignedInUser(signedInUser.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    assertNotNull(either.b)
                    either.b!!.matches(credentials, user)
                }
    }

    @Test
    fun completeSignUp_shouldPropagateTheNoInternetConnectionException() {

        fun go() {

            val user = completedUser()

            manager.completeSignUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.CompleteSignUpException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = true
        imageRepository.hasInternet = false
        go()

        remoteRepository.hasInternet = false
        imageRepository.hasInternet = true
        go()

        remoteRepository.hasInternet = false
        imageRepository.hasInternet = false
        go()
    }

    @Test
    fun completeSignUp_shouldPropagateTheNoSignedInUserException() {

        fun go() {

            val user = completedUser()

            manager.completeSignUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.CompleteSignUpException.NoSignedInUserException
            }
        }

        remoteRepository.isUserSignedIn = true
        imageRepository.isUserSignedIn = false
        go()

        remoteRepository.isUserSignedIn = false
        imageRepository.isUserSignedIn = true
        go()

        remoteRepository.isUserSignedIn = false
        imageRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun completeSignUp_shouldPropagateTheInternalException() {

        fun go() {

            val user = completedUser()

            manager.completeSignUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.CompleteSignUpException.InternalException
            }
        }

        imageRepository.triggerInternalException = true
        go()
    }

    @Test
    fun completeSignUp_shouldPropagateTheUnknownException() {

        fun go() {

            val user = completedUser()

            manager.completeSignUp(user).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.CompleteSignUpException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        imageRepository.triggerUnknownException = false
        go()

        remoteRepository.triggerUnknownException = false
        imageRepository.triggerUnknownException = true
        go()

        remoteRepository.triggerUnknownException = true
        imageRepository.triggerUnknownException = true
        go()
    }

    @Test
    fun signInWithGoogle_shouldSignTheUserUpAndInAsIncompleteUserIfTheUserIsNotRegistered() {

        fun go() {

            val incomplete = manager.signInWithGoogle()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b.isLeft()
                    }.values()[0].orNull()!!.swap().orNull()!!

            authenticator.observeSignedInUser()
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(incomplete.right())

            remoteRepository.findSignedInUser(incomplete.id)
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b == null
                    }
        }

        assertEquals(authenticator.googleUsers.size, 0)
        go()
        assertEquals(authenticator.googleUsers.size, 1)
        go()
        assertEquals(authenticator.googleUsers.size, 2)
    }

    @Test
    fun signInWithGoogle_shouldSignTheUserInAsIncompleteUserIfTheUserIsRegisteredButNotCompleted() {

        fun go() {

            val incomplete = manager.signInWithGoogle()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b.isLeft()
                    }.values()[0].orNull()!!.swap().orNull()!!

            authenticator.observeSignedInUser()
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(incomplete.right())

            remoteRepository.findSignedInUser(incomplete.id)
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b == null
                    }

            authenticator.signOut().test().await().assertNoErrors().assertComplete()
        }

        val user = incompleteUser()
        authenticator.providersSignInUserFactory = { user }

        assertEquals(authenticator.googleUsers.size, 0)
        go()
        assertEquals(authenticator.googleUsers.size, 1)
        go()
        assertEquals(authenticator.googleUsers.size, 1)
    }

    @Test
    fun signInWithGoogle_shouldSignTheUserInAsSignedInUserIfTheUserIsRegisteredAndCompleted() {

        val user = incompleteUser()
        authenticator.providersSignInUserFactory = { user }

        assertEquals(authenticator.googleUsers.size, 0)

        val incomplete = manager.signInWithGoogle()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.isLeft()
                }.values()[0].orNull()!!.swap().orNull()!!

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.right())

        remoteRepository.findSignedInUser(incomplete.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b == null
                }

        val completed = remoteSignUpUser(incomplete.id, incomplete.email!!)

        remoteRepository.storeSignUpUser(completed)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        assertEquals(authenticator.googleUsers.size, 1)

        authenticator.signOut().test().await().assertNoErrors().assertComplete()

        val signedIn = manager.signInWithGoogle()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.isRight()
                }.values()[0].orNull()!!.orNull()!!

        assertEquals(completed.toSignedInUser(signedIn.timestamp), signedIn)
        assertEquals(authenticator.googleUsers.size, 1)

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.right())

        remoteRepository.findSignedInUser(incomplete.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b == signedIn
                }
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheAccountHasBeenDisabledException() {

        fun go() {
            manager.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithGoogleException.AccountHasBeenDisabledException
            }
        }

        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.AccountHasBeenDisabledException
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheMalformedOrExpiredCredentialException() {

        fun go() {
            manager.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithGoogleException.MalformedOrExpiredCredentialException
            }
        }

        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.MalformedOrExpiredCredentialException
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheEmailAlreadyInUseException() {

        fun go() {
            manager.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithGoogleException.EmailAlreadyInUseException
            }
        }

        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.EmailAlreadyInUseException
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheNoResponseException() {

        fun go() {
            manager.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithGoogleException.NoResponseException
            }
        }

        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.NoResponseException
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            manager.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithGoogleException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = true
        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.NoInternetConnectionException
        go()

        remoteRepository.hasInternet = false
        authenticator.signInWithGoogleException = null
        go()

        remoteRepository.hasInternet = false
        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.NoInternetConnectionException
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheInternalException() {

        fun go() {
            manager.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithGoogleException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheUnknownException() {

        fun go() {
            manager.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithGoogleException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = false
        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.UnknownException(RuntimeException(), "Google")
        go()

        remoteRepository.triggerUnknownException = true
        authenticator.signInWithGoogleException = null
        go()

        remoteRepository.triggerUnknownException = true
        authenticator.signInWithGoogleException =
                Authenticator.SignInWithGoogleException.UnknownException(RuntimeException(), "Google")
        go()
    }

    @Test
    fun signInWithFacebook_shouldSignTheUserUpAndInAsIncompleteUserIfTheUserIsNotRegistered() {

        fun go() {

            val incomplete = manager.signInWithFacebook()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b.isLeft()
                    }.values()[0].orNull()!!.swap().orNull()!!

            authenticator.observeSignedInUser()
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(incomplete.right())

            remoteRepository.findSignedInUser(incomplete.id)
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b == null
                    }
        }

        assertEquals(authenticator.facebookUsers.size, 0)
        go()
        assertEquals(authenticator.facebookUsers.size, 1)
        go()
        assertEquals(authenticator.facebookUsers.size, 2)
    }

    @Test
    fun signInWithFacebook_shouldSignTheUserInAsIncompleteUserIfTheUserIsRegisteredButNotCompleted() {

        fun go() {

            val incomplete = manager.signInWithFacebook()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b.isLeft()
                    }.values()[0].orNull()!!.swap().orNull()!!

            authenticator.observeSignedInUser()
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(incomplete.right())

            remoteRepository.findSignedInUser(incomplete.id)
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b == null
                    }

            authenticator.signOut().test().await().assertNoErrors().assertComplete()
        }

        val user = incompleteUser()
        authenticator.providersSignInUserFactory = { user }

        assertEquals(authenticator.facebookUsers.size, 0)
        go()
        assertEquals(authenticator.facebookUsers.size, 1)
        go()
        assertEquals(authenticator.facebookUsers.size, 1)
    }

    @Test
    fun signInWithFacebook_shouldSignTheUserInAsSignedInUserIfTheUserIsRegisteredAndCompleted() {

        val user = incompleteUser()
        authenticator.providersSignInUserFactory = { user }

        assertEquals(authenticator.facebookUsers.size, 0)

        val incomplete = manager.signInWithFacebook()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.isLeft()
                }.values()[0].orNull()!!.swap().orNull()!!

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.right())

        remoteRepository.findSignedInUser(incomplete.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b == null
                }

        val completed = remoteSignUpUser(incomplete.id, incomplete.email!!)

        remoteRepository.storeSignUpUser(completed)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        assertEquals(authenticator.facebookUsers.size, 1)

        authenticator.signOut().test().await().assertNoErrors().assertComplete()

        val signedIn = manager.signInWithFacebook()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.isRight()
                }.values()[0].orNull()!!.orNull()!!

        assertEquals(completed.toSignedInUser(signedIn.timestamp), signedIn)
        assertEquals(authenticator.facebookUsers.size, 1)

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.right())

        remoteRepository.findSignedInUser(incomplete.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b == signedIn
                }
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheAccountHasBeenDisabledException() {

        fun go() {
            manager.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithFacebookException.AccountHasBeenDisabledException
            }
        }

        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.AccountHasBeenDisabledException
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheMalformedOrExpiredCredentialException() {

        fun go() {
            manager.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithFacebookException.MalformedOrExpiredCredentialException
            }
        }

        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.MalformedOrExpiredCredentialException
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheEmailAlreadyInUseException() {

        fun go() {
            manager.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithFacebookException.EmailAlreadyInUseException
            }
        }

        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.EmailAlreadyInUseException
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheNoResponseException() {

        fun go() {
            manager.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithFacebookException.NoResponseException
            }
        }

        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.NoResponseException
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            manager.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithFacebookException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = true
        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.NoInternetConnectionException
        go()

        remoteRepository.hasInternet = false
        authenticator.signInWithFacebookException = null
        go()

        remoteRepository.hasInternet = false
        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.NoInternetConnectionException
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheInternalException() {

        fun go() {
            manager.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithFacebookException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheUnknownException() {

        fun go() {
            manager.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithFacebookException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = false
        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.UnknownException(RuntimeException(), "Facebook")
        go()

        remoteRepository.triggerUnknownException = true
        authenticator.signInWithFacebookException = null
        go()

        remoteRepository.triggerUnknownException = true
        authenticator.signInWithFacebookException =
                Authenticator.SignInWithFacebookException.UnknownException(RuntimeException(), "Facebook")
        go()
    }

    @Test
    fun signInWithTwitter_shouldSignTheUserUpAndInAsIncompleteUserIfTheUserIsNotRegistered() {

        fun go() {

            val incomplete = manager.signInWithTwitter()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b.isLeft()
                    }.values()[0].orNull()!!.swap().orNull()!!

            authenticator.observeSignedInUser()
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(incomplete.right())

            remoteRepository.findSignedInUser(incomplete.id)
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b == null
                    }
        }

        assertEquals(authenticator.twitterUsers.size, 0)
        go()
        assertEquals(authenticator.twitterUsers.size, 1)
        go()
        assertEquals(authenticator.twitterUsers.size, 2)
    }

    @Test
    fun signInWithTwitter_shouldSignTheUserInAsIncompleteUserIfTheUserIsRegisteredButNotCompleted() {

        fun go() {

            val incomplete = manager.signInWithTwitter()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b.isLeft()
                    }.values()[0].orNull()!!.swap().orNull()!!

            authenticator.observeSignedInUser()
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue(incomplete.right())

            remoteRepository.findSignedInUser(incomplete.id)
                    .firstOrError()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()
                    .assertValue { either ->
                        assertTrue(either.isRight())
                        either as Either.Right
                        either.b == null
                    }

            authenticator.signOut().test().await().assertNoErrors().assertComplete()
        }

        val user = incompleteUser()
        authenticator.providersSignInUserFactory = { user }

        assertEquals(authenticator.twitterUsers.size, 0)
        go()
        assertEquals(authenticator.twitterUsers.size, 1)
        go()
        assertEquals(authenticator.twitterUsers.size, 1)
    }

    @Test
    fun signInWithTwitter_shouldSignTheUserInAsSignedInUserIfTheUserIsRegisteredAndCompleted() {

        val user = incompleteUser()
        authenticator.providersSignInUserFactory = { user }

        assertEquals(authenticator.twitterUsers.size, 0)

        val incomplete = manager.signInWithTwitter()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.isLeft()
                }.values()[0].orNull()!!.swap().orNull()!!

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.right())

        remoteRepository.findSignedInUser(incomplete.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b == null
                }

        val completed = remoteSignUpUser(incomplete.id, incomplete.email!!)

        remoteRepository.storeSignUpUser(completed)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()

        assertEquals(authenticator.twitterUsers.size, 1)

        authenticator.signOut().test().await().assertNoErrors().assertComplete()

        val signedIn = manager.signInWithTwitter()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b.isRight()
                }.values()[0].orNull()!!.orNull()!!

        assertEquals(completed.toSignedInUser(signedIn.timestamp), signedIn)
        assertEquals(authenticator.twitterUsers.size, 1)

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.right())

        remoteRepository.findSignedInUser(incomplete.id)
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue { either ->
                    assertTrue(either.isRight())
                    either as Either.Right
                    either.b == signedIn
                }
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheAccountHasBeenDisabledException() {

        fun go() {
            manager.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithTwitterException.AccountHasBeenDisabledException
            }
        }

        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.AccountHasBeenDisabledException
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheMalformedOrExpiredCredentialException() {

        fun go() {
            manager.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithTwitterException.MalformedOrExpiredCredentialException
            }
        }

        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.MalformedOrExpiredCredentialException
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheEmailAlreadyInUseException() {

        fun go() {
            manager.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithTwitterException.EmailAlreadyInUseException
            }
        }

        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.EmailAlreadyInUseException
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheNoResponseException() {

        fun go() {
            manager.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithTwitterException.NoResponseException
            }
        }

        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.NoResponseException
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            manager.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithTwitterException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = true
        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.NoInternetConnectionException
        go()

        remoteRepository.hasInternet = false
        authenticator.signInWithTwitterException = null
        go()

        remoteRepository.hasInternet = false
        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.NoInternetConnectionException
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheInternalException() {

        fun go() {
            manager.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithTwitterException.InternalException
            }
        }

        remoteRepository.triggerInternalException = true
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheUnknownException() {

        fun go() {
            manager.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignInWithTwitterException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = false
        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.UnknownException(RuntimeException(), "Twitter")
        go()

        remoteRepository.triggerUnknownException = true
        authenticator.signInWithTwitterException = null
        go()

        remoteRepository.triggerUnknownException = true
        authenticator.signInWithTwitterException =
                Authenticator.SignInWithTwitterException.UnknownException(RuntimeException(), "Twitter")
        go()
    }

    @Test
    fun sendPasswordResetEmail_shouldSendPasswordResetEmailToTheGivenEmail() {

        assertEquals(authenticator.resetEmailsReceivers.size, 0)

        manager.sendPasswordResetEmail(randomEmail())
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(Unit.right())

        assertEquals(authenticator.resetEmailsReceivers.size, 1)
    }

    @Test
    fun sendPasswordResetEmail_shouldPropagateTheNonExistentEmailException() {

        val email = randomEmail()

        fun go() {
            manager.sendPasswordResetEmail(email).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                val e = it.a
                e as AuthManager.SendPasswordResetEmailException.NonExistentEmailException
                e.email == email
            }
        }

        authenticator.sendPasswordResetEmailException =
                Authenticator.SendPasswordResetEmailException.NonExistentEmailException(email)
        go()
    }

    @Test
    fun sendPasswordResetEmail_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            manager.sendPasswordResetEmail(randomEmail()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SendPasswordResetEmailException.NoInternetConnectionException
            }
        }

        authenticator.sendPasswordResetEmailException =
                Authenticator.SendPasswordResetEmailException.NoInternetConnectionException
        go()
    }

    @Test
    fun sendPasswordResetEmail_shouldPropagateTheUnknownException() {

        fun go() {
            manager.sendPasswordResetEmail(randomEmail()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SendPasswordResetEmailException.UnknownException
            }
        }

        authenticator.sendPasswordResetEmailException =
                Authenticator.SendPasswordResetEmailException.UnknownException(RuntimeException())
        go()
    }

    @Test
    fun signOut_shouldSignTheUserOut() {

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())

        manager.signOut()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(Unit.right())

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())

        val incomplete = authenticator.signInWithGoogle()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .values()[0].orNull()!!

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(incomplete.right())

        manager.signOut()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(Unit.right())

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())
    }

    @Test
    fun signOut_shouldPropagateTheNoInternetConnectionException() {

        fun go() {

            authenticator.signInWithGoogle()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()

            manager.signOut().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignOutException.NoInternetConnectionException
            }
        }

        remoteRepository.hasInternet = false
        authenticator.signOutException = null
        authenticator.observeSignedInUserException = null
        go()

        remoteRepository.hasInternet = true
        authenticator.signOutException =
                Authenticator.SignOutException.NoInternetConnectionException
        authenticator.observeSignedInUserException = null
        go()

        remoteRepository.hasInternet = true
        authenticator.signOutException = null
        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.NoInternetConnectionException
        go()

        remoteRepository.hasInternet = false
        authenticator.signOutException =
                Authenticator.SignOutException.NoInternetConnectionException
        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.NoInternetConnectionException
        go()
    }

    @Test
    fun signOut_shouldConvertNoSignedInUserExceptionsToInternalExceptions() {

        fun go() {

            authenticator.signInWithGoogle()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()

            manager.signOut().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignOutException.InternalException
            }
        }

        remoteRepository.isUserSignedIn = false
        go()
    }

    @Test
    fun signOut_shouldPropagateTheUnknownException() {

        fun go() {

            authenticator.signInWithGoogle()
                    .test()
                    .await()
                    .assertNoErrors()
                    .assertComplete()

            manager.signOut().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is AuthManager.SignOutException.UnknownException
            }
        }

        remoteRepository.triggerUnknownException = true
        authenticator.signOutException = null
        authenticator.observeSignedInUserException = null
        go()

        remoteRepository.triggerUnknownException = false
        authenticator.signOutException =
                Authenticator.SignOutException.UnknownException(RuntimeException())
        authenticator.observeSignedInUserException = null
        go()

        remoteRepository.triggerUnknownException = false
        authenticator.signOutException = null
        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.UnknownException(RuntimeException())
        go()

        remoteRepository.triggerUnknownException = true
        authenticator.signOutException =
                Authenticator.SignOutException.UnknownException(RuntimeException())
        authenticator.observeSignedInUserException =
                Authenticator.ObserveSignedInUserException.UnknownException(RuntimeException())
        go()
    }
}

private fun SignedInUser.matches(signUpUser: SignUpUser): Boolean {
    return signUpUser.credentials.email == this.email &&
            signUpUser.displayName == this.displayName &&
            signUpUser.phoneNumber == this.phoneNumber
}

private fun RemoteSignUpUser.matches(retrieved: SimpleRetrievedUser): Boolean {
    return retrieved.id == this.id &&
            retrieved.displayName == this.displayName &&
            retrieved.pictureUrl == this.pictureUrl
}

private fun IncompleteUser.matches(credentials: UserCredentials): Boolean {
    return credentials.email == this.email
}

private fun SignedInUser.matches(
        credentials: UserCredentials,
        remoteSignUpUser: RemoteSignUpUser
): Boolean {
    return credentials.email == this.email &&
            remoteSignUpUser.email == this.email &&
            remoteSignUpUser.username == this.username &&
            remoteSignUpUser.displayName == this.displayName &&
            remoteSignUpUser.phoneNumber == this.phoneNumber &&
            remoteSignUpUser.pictureUrl == this.pictureUrl
}

private fun SignedInUser.matches(
        credentials: UserCredentials,
        completedUser: CompletedUser
): Boolean {

    val pictureMatches = completedUser.picture?.fold(ifLeft = {
        this.pictureUrl == it
    }, ifRight = {
        this.pictureUrl != null
    }) ?: (this.pictureUrl == null)

    return credentials.email == this.email &&
            completedUser.email == this.email &&
            completedUser.displayName == this.displayName &&
            completedUser.phoneNumber == this.phoneNumber &&
            pictureMatches
}

private fun UserCredentials.toIncompleteUser(id: UserId): IncompleteUser {
    return IncompleteUser.of(
            id,
            this.email,
            null,
            null,
            null
    )
}
