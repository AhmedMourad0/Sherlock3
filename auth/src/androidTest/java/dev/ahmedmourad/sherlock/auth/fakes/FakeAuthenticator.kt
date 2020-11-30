package dev.ahmedmourad.sherlock.auth.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

internal class FakeAuthenticator(
        var providersSignInUserFactory: () -> IncompleteUser
) : Authenticator {

    val googleUsers = mutableListOf<IncompleteUser>()
    val twitterUsers = mutableListOf<IncompleteUser>()
    val facebookUsers = mutableListOf<IncompleteUser>()
    private val regularUsers = mutableMapOf<UserCredentials, IncompleteUser>()
    private var signedInUser: IncompleteUser? = null
    val resetEmailsReceivers = mutableListOf<Email>()

    var observeUserAuthStateException: Authenticator.ObserveUserAuthStateException? = null
    var observeSignedInUserException: Authenticator.ObserveSignedInUserException? = null
    var signInException: Authenticator.SignInException? = null
    var signUpException: Authenticator.SignUpException? = null
    var signInWithGoogleException: Authenticator.SignInWithGoogleException? = null
    var signInWithFacebookException: Authenticator.SignInWithFacebookException? = null
    var signInWithTwitterException: Authenticator.SignInWithTwitterException? = null
    var sendPasswordResetEmailException: Authenticator.SendPasswordResetEmailException? = null
    var signOutException: Authenticator.SignOutException? = null

    override fun observeUserAuthState():
            Flowable<Either<Authenticator.ObserveUserAuthStateException, Boolean>> {
        return Single.fromCallable {
            val e = observeUserAuthStateException
            e?.left() ?: (signedInUser != null).right()
        }.toFlowable()
    }

    override fun observeSignedInUser():
            Flowable<Either<Authenticator.ObserveSignedInUserException, IncompleteUser?>> {
        return Single.fromCallable {
            val e = observeSignedInUserException
            e?.left() ?: signedInUser.right()
        }.toFlowable()
    }

    override fun signIn(
            credentials: UserCredentials
    ): Single<Either<Authenticator.SignInException, IncompleteUser>> {
        return Single.fromCallable {

            val e = signInException
            if (e == null) {

                val emailOnlyMatches = regularUsers.filter { (c, _) -> c.email == credentials.email }
                val match = emailOnlyMatches[credentials]

                when {

                    match != null -> {
                        signedInUser = match
                        match.right()
                    }

                    emailOnlyMatches.isNotEmpty() ->
                        Authenticator.SignInException.WrongPasswordException.left()

                    else ->
                        Authenticator.SignInException.AccountDoesNotExistOrHasBeenDisabledException.left()
                }

            } else {
                e.left()
            }
        }
    }

    override fun signUp(
            credentials: UserCredentials
    ): Single<Either<Authenticator.SignUpException, IncompleteUser>> {
        return Single.fromCallable {
            val e = signUpException
            if (e == null) {
                val user = IncompleteUser.of(
                        UserId(UUID.randomUUID().toString()),
                        credentials.email,
                        null,
                        null,
                        null
                )
                regularUsers[credentials] = user
                signedInUser = user
                user.right()
            } else {
                e.left()
            }
        }
    }

    override fun signInWithGoogle():
            Single<Either<Authenticator.SignInWithGoogleException, IncompleteUser>> {
        return Single.fromCallable {
            val e = signInWithGoogleException
            if (e == null) {
                val user = providersSignInUserFactory.invoke()
                if (!googleUsers.contains(user)) {
                    googleUsers.add(user)
                }
                signedInUser = user
                user.right()
            } else {
                e.left()
            }
        }
    }

    override fun signInWithFacebook():
            Single<Either<Authenticator.SignInWithFacebookException, IncompleteUser>> {
        return Single.fromCallable {
            val e = signInWithFacebookException
            if (e == null) {
                val user = providersSignInUserFactory.invoke()
                if (!facebookUsers.contains(user)) {
                    facebookUsers.add(user)
                }
                signedInUser = user
                user.right()
            } else {
                e.left()
            }
        }
    }

    override fun signInWithTwitter():
            Single<Either<Authenticator.SignInWithTwitterException, IncompleteUser>> {
        return Single.fromCallable {
            val e = signInWithTwitterException
            if (e == null) {
                val user = providersSignInUserFactory.invoke()
                if (!twitterUsers.contains(user)) {
                    twitterUsers.add(user)
                }
                signedInUser = user
                user.right()
            } else {
                e.left()
            }
        }
    }

    override fun sendPasswordResetEmail(
            email: Email
    ): Single<Either<Authenticator.SendPasswordResetEmailException, Unit>> {
        return Single.fromCallable {
            val e = sendPasswordResetEmailException
            if (e == null) {
                resetEmailsReceivers.add(email)
                Unit.right()
            } else {
                e.left()
            }
        }
    }

    override fun signOut(): Single<Either<Authenticator.SignOutException, UserId?>> {
        return Single.fromCallable {
            val e = signOutException
            if (e == null) {
                val id = signedInUser?.id
                signedInUser = null
                id.right()
            } else {
                e.left()
            }
        }
    }
}
