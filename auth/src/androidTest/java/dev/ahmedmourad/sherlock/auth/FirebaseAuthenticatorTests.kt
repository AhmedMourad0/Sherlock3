package dev.ahmedmourad.sherlock.auth

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import arrow.core.Either
import arrow.core.left
import arrow.core.orNull
import arrow.core.right
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import dev.ahmedmourad.sherlock.auth.authenticator.FirebaseAuthenticator
import dev.ahmedmourad.sherlock.auth.authenticator.toIncompleteUser
import dev.ahmedmourad.sherlock.auth.fakes.FakeCloudMessenger
import dev.ahmedmourad.sherlock.auth.fakes.FakeConnectivityManager
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.auth.utils.*
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import inc.ahmedmourad.sherlock.auth.R
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import splitties.init.appCtx
import timber.log.LogcatTree
import timber.log.Timber
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class FirebaseAuthenticatorTests {

    private lateinit var connectivityManager: FakeConnectivityManager
    private lateinit var messenger: FakeCloudMessenger

    private lateinit var auth: FirebaseAuth

    private lateinit var authenticator: FirebaseAuthenticator

    @Before
    fun setup() {

        Timber.plant(LogcatTree("Sherlock"))

        val initializeFirebase = FirebaseApp.getApps(appCtx).isEmpty()

        if (initializeFirebase) {
            FirebaseApp.initializeApp(appCtx,
                    FirebaseOptions.Builder()
                            .setApplicationId(appCtx.getString(R.string.firebase_application_id))
                            .setApiKey(appCtx.getString(R.string.firebase_api_key))
                            .setProjectId(appCtx.getString(R.string.firebase_project_id))
                            .setDatabaseUrl(appCtx.getString(R.string.firebase_database_url))
                            .setStorageBucket(appCtx.getString(R.string.firebase_storage_bucket))
                            .setGcmSenderId(appCtx.getString(R.string.firebase_gcm_sender_id))
                            .build()
            )
        }

        auth = FirebaseAuth.getInstance().apply {
            if (initializeFirebase) {
                useEmulator(appCtx.getString(R.string.local_ip_address), 9099)
            }
        }

        messenger = FakeCloudMessenger()
        connectivityManager = FakeConnectivityManager()

        authenticator = FirebaseAuthenticator(
                auth = { auth },
                messenger = { messenger },
                connectivityManager = { connectivityManager }
        )

        auth.signOut()
        flushAll()
    }

    @Test
    fun observeUserAuthState_shouldReturnWhetherThereIsSignedInUserOrNot() {

        authenticator.observeUserAuthState()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(false.right())

        signInAnonymously()

        authenticator.observeUserAuthState()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(true.right())

        auth.signOut()

        authenticator.observeUserAuthState()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(false.right())
    }

    @Test
    fun observeUserAuthState_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.observeUserAuthState().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.ObserveUserAuthStateException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun observeUserAuthState_shouldPropagateTheUnknownException() {

        fun go() {
            authenticator.observeUserAuthState().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.ObserveUserAuthStateException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        go()
    }

    @Test
    fun observeSignedInUser_shouldReturnTheCurrentSignedInUserOrNullIfThereIsNotOne() {

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())

        val credentials = userCredentials()
        val (_, uid) = signUp(credentials)

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(credentials.toIncompleteUser(uid).right())

        auth.signOut()

        authenticator.observeSignedInUser()
                .firstOrError()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())
    }

    @Test
    fun observeSignedInUser_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.observeSignedInUser().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.ObserveSignedInUserException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun observeSignedInUser_shouldPropagateTheUnknownException() {

        fun go() {
            authenticator.observeSignedInUser().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.ObserveSignedInUserException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        go()
    }

    @Test
    fun signIn_shouldSignTheUserInIfTheUserIsSignedUp() {

        val credentials = userCredentials()
        val (_, uid) = signUp(credentials)
        auth.signOut()

        assertEquals(null, auth.currentUser?.toIncompleteUser())

        authenticator.signIn(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(credentials.toIncompleteUser(uid).right())

        assertEquals(credentials.toIncompleteUser(uid), auth.currentUser?.toIncompleteUser())
    }

    @Test
    fun signIn_shouldSubscribeToTheMessagingTopicOfTheUserId() {

        val credentials = userCredentials()
        val (_, uid) = signUp(credentials)
        auth.signOut()

        assertEquals(0, messenger.subscriptions.size)

        authenticator.signIn(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(credentials.toIncompleteUser(uid).right())

        assertEquals(1, messenger.subscriptions.size)
        assertEquals(uid.value, messenger.subscriptions[0])
    }

    @Test
    fun signIn_shouldReturnAccountDoesNotExistOrHasBeenDisabledExceptionIfTheUserHasNotSignedUp() {
        authenticator.signIn(userCredentials()).test().await().assertValue {
            assertTrue(it is Either.Left)
            it as Either.Left
            it.a == Authenticator.SignInException.AccountDoesNotExistOrHasBeenDisabledException
        }
    }

    @Test
    fun signIn_shouldReturnWrongPasswordExceptionIfTheEmailIsCorrectButThePasswordIsNot() {

        val credentials = userCredentials()
        signUp(credentials)
        auth.signOut()

        val wrongPasswordCredentials = UserCredentials.of(
                credentials.email,
                randomPassword()
        ).orNull()!!

        authenticator.signIn(wrongPasswordCredentials).test().await().assertValue {
            assertTrue(it is Either.Left)
            it as Either.Left
            it.a == Authenticator.SignInException.WrongPasswordException
        }
    }

    @Test
    fun signIn_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.signIn(userCredentials()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.SignInException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun signIn_shouldPropagateTheUnknownException() {

        val credentials = userCredentials()
        signUp(credentials)
        auth.signOut()

        fun go() {
            authenticator.signIn(credentials).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.SignInException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        messenger.fail = false
        go()

        connectivityManager.triggerUnknownException = false
        messenger.fail = true
        go()

        connectivityManager.triggerUnknownException = true
        messenger.fail = true
        go()
    }

    @Test
    fun signUp_shouldSignTheUserUpAndIn() {

        val credentials = userCredentials()

        assertEquals(null, auth.currentUser?.toIncompleteUser())

        val incomplete = authenticator.signUp(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .values()[0]
                .orNull()!!

        assertEquals(credentials.toIncompleteUser(incomplete.id), incomplete)
        assertEquals(credentials.toIncompleteUser(incomplete.id), auth.currentUser?.toIncompleteUser())
    }

    @Test
    fun signUp_shouldSubscribeToTheMessagingTopicOfTheUserId() {

        val credentials = userCredentials()

        assertEquals(0, messenger.subscriptions.size)

        val incomplete = authenticator.signUp(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .values()[0]
                .orNull()!!

        assertEquals(credentials.toIncompleteUser(incomplete.id), incomplete)

        assertEquals(1, messenger.subscriptions.size)
        assertEquals(incomplete.id.value, messenger.subscriptions[0])
    }

    @Test
    fun signUp_shouldReturnEmailAlreadyInUseExceptionIfTheEmailIsAssociatedWithAnotherAccount() {

        val credentials = userCredentials()

        val incomplete = authenticator.signUp(credentials)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .values()[0]
                .orNull()!!

        assertEquals(credentials.toIncompleteUser(incomplete.id), incomplete)

        auth.signOut()

        val sameEmailCredentials = UserCredentials.of(
                credentials.email,
                randomPassword()
        ).orNull()!!

        authenticator.signUp(sameEmailCredentials).test().await().assertValue {
            assertTrue(it is Either.Left)
            it as Either.Left
            it.a == Authenticator.SignUpException.EmailAlreadyInUseException(credentials.email)
        }
    }

    @Test
    fun signUp_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.signUp(userCredentials()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.SignUpException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun signUp_shouldPropagateTheUnknownException() {

        fun go() {
            authenticator.signUp(userCredentials()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.SignUpException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        messenger.fail = false
        go()

        connectivityManager.triggerUnknownException = false
        messenger.fail = true
        go()

        connectivityManager.triggerUnknownException = true
        messenger.fail = true
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.SignInWithGoogleException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun signInWithGoogle_shouldPropagateTheUnknownException() {

        fun go() {
            authenticator.signInWithGoogle().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.SignInWithGoogleException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.SignInWithFacebookException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun signInWithFacebook_shouldPropagateTheUnknownException() {

        fun go() {
            authenticator.signInWithFacebook().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.SignInWithFacebookException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.SignInWithTwitterException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun signInWithTwitter_shouldPropagateTheUnknownException() {

        fun go() {
            authenticator.signInWithTwitter().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.SignInWithTwitterException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        go()
    }

    @Test
    fun sendPasswordResetEmail_shouldReturnUnitInCaseOfSuccess() {

        val credentials = userCredentials()
        signUp(credentials)
        auth.signOut()

        authenticator.sendPasswordResetEmail(credentials.email)
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(Unit.right())
    }

    @Test
    fun sendPasswordResetEmail_shouldReturnNonExistentEmailExceptionIfTheEmailDoesNotExists() {
        val email = randomEmail()
        authenticator.sendPasswordResetEmail(email).test().await().assertValue {
            assertTrue(it is Either.Left)
            it as Either.Left
            it.a == Authenticator.SendPasswordResetEmailException.NonExistentEmailException(email)
        }
    }

    @Test
    fun sendPasswordResetEmail_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.sendPasswordResetEmail(randomEmail()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.SendPasswordResetEmailException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun sendPasswordResetEmail_shouldPropagateTheUnknownException() {

        fun go() {
            authenticator.sendPasswordResetEmail(randomEmail()).test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.SendPasswordResetEmailException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        go()
    }

    @Test
    fun signOut_shouldSignTheUserOutAndReturnTheirUserId() {

        val credentials = userCredentials()
        val (_, uid) = signUp(credentials)

        assertEquals(credentials.toIncompleteUser(uid), auth.currentUser?.toIncompleteUser())

        authenticator.signOut()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(uid.right())

        assertEquals(null, auth.currentUser?.toIncompleteUser())
    }

    @Test
    fun signOut_shouldReturnNullIfThereIsNoSignedInUser() {
        authenticator.signOut()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(null.right())
    }

    @Test
    fun signOut_shouldUnsubscribeFromTheMessagingTopicOfTheUserId() {

        val credentials = userCredentials()
        val (_, uid) = signUp(credentials)

        messenger.subscribe(uid.value).test().await().assertNoErrors().assertComplete()

        assertEquals(1, messenger.subscriptions.size)
        assertEquals(uid.value, messenger.subscriptions[0])

        authenticator.signOut()
                .test()
                .await()
                .assertNoErrors()
                .assertComplete()
                .assertValue(uid.right())

        assertEquals(0, messenger.subscriptions.size)
    }

    @Test
    fun signOut_shouldPropagateTheNoInternetConnectionException() {

        fun go() {
            authenticator.signOut().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a == Authenticator.SignOutException.NoInternetConnectionException
            }
        }

        connectivityManager.hasInternet = false
        go()
    }

    @Test
    fun signOut_shouldPropagateTheUnknownException() {

        fun go() {

            signInAnonymously()

            authenticator.signOut().test().await().assertValue {
                assertTrue(it is Either.Left)
                it as Either.Left
                it.a is Authenticator.SignOutException.UnknownException
            }
        }

        connectivityManager.triggerUnknownException = true
        messenger.fail = false
        go()

        connectivityManager.triggerUnknownException = false
        messenger.fail = true
        go()

        connectivityManager.triggerUnknownException = true
        messenger.fail = true
        go()
    }

    private fun flushAll() {
        val projectId = appCtx.getString(R.string.firebase_project_id)
        val ipAddress = appCtx.getString(R.string.local_ip_address)
        flush("http://$ipAddress:8080/emulator/v1/projects/$projectId/databases/(default)/documents")
        val (token, _) = signInAnonymously()
        flush(url = "http://$ipAddress:9099/emulator/v1/projects/$projectId/accounts",
                headers = mapOf("Authorization" to "Bearer ${token.value}")
        )
        auth.signOut()
    }

    private fun flush(url: String, headers: Map<String, String>? = null) {

        val request = Request.Builder()
                .url(url)
                .apply {
                    headers?.forEach { (name, value) ->
                        this.addHeader(name, value)
                    }
                }.delete()
                .build()

        OkHttpClient().newCall(request).execute()
    }

    private fun signInAnonymously(): Pair<FirebaseToken, UserId> {
        return Single.create<Either<Throwable, Pair<FirebaseToken, UserId>>> { emitter ->
            auth.signInAnonymously().onSuccessTask { result ->
                result!!.user!!.getIdToken(true).continueWith {
                    FirebaseToken(it.result!!.token!!) to UserId(result.user!!.uid)
                }
            }.addOnSuccessListener {
                emitter.onSuccess(it.right())
            }.addOnFailureListener {
                emitter.onSuccess(it.left())
            }
        }.blockingGet().orNull()!!
    }

    private fun signUp(
            credentials: UserCredentials
    ): Pair<FirebaseToken, UserId> {
        return Single.create<Either<Throwable, Pair<FirebaseToken, UserId>>> { emitter ->
            auth.createUserWithEmailAndPassword(
                    credentials.email.value,
                    credentials.password.value
            ).onSuccessTask { result ->
                result!!.user!!.getIdToken(true).continueWith {
                    FirebaseToken(it.result!!.token!!) to UserId(result.user!!.uid)
                }
            }.addOnSuccessListener {
                emitter.onSuccess(it.right())
            }.addOnFailureListener {
                emitter.onSuccess(it.left())
            }
        }.blockingGet().orNull()!!
    }
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
