package dev.ahmedmourad.sherlock.auth.authenticator

import android.content.Intent
import android.util.Log
import arrow.core.*
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.twitter.sdk.android.core.*
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.authenticator.activity.AuthSignInActivity
import dev.ahmedmourad.sherlock.auth.authenticator.bus.AuthenticatorBus
import dev.ahmedmourad.sherlock.auth.di.InternalApi
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import inc.ahmedmourad.sherlock.auth.R
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

private class AuthUserCollisionException(
        val attemptedCredential: AuthCredential,
        val origin: FirebaseAuthUserCollisionException
) : Exception()

@Reusable
internal class FirebaseAuthenticator @Inject constructor(
        @InternalApi private val auth: Lazy<FirebaseAuth>,
        private val connectivityManager: Lazy<ConnectivityManager>
) : Authenticator {

    init {
        val config = TwitterConfig.Builder(appCtx)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig(
                        appCtx.getString(R.string.twitter_api_key),
                        appCtx.getString(R.string.twitter_api_key_secret)
                )).debug(BuildConfig.DEBUG).build()
        Twitter.initialize(config)
    }

    override fun observeUserAuthState(): Flowable<Boolean> {
        return createObserveUserAuthState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    private fun createObserveUserAuthState(): Flowable<Boolean> {
        return Flowable.create<Boolean>({ emitter ->

            val authStateListener = { firebaseAuth: FirebaseAuth ->
                emitter.onNext(firebaseAuth.currentUser != null)
            }

            emitter.setCancellable { auth.get().removeAuthStateListener(authStateListener) }

            emitter.onNext(auth.get().currentUser != null)

            auth.get().addAuthStateListener(authStateListener)

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun observeCurrentUser(): Flowable<Option<IncompleteUser>> {
        return observeUserAuthState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .switchMap { isUserSignedIn ->
                    if (isUserSignedIn) {
                        Flowable.just(auth.get().currentUser?.toIncompleteUser().toOption())
                    } else {
                        Flowable.just(none())
                    }
                }
    }

    override fun signIn(
            credentials: UserCredentials
    ): Single<Either<Authenticator.SignInException, IncompleteUser>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                Authenticator.SignInException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            createSignIn(credentials.email, credentials.password)
                        } else {
                            Single.just(
                                    Authenticator.SignInException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }
    }

    private fun createSignIn(
            email: Email,
            password: Password
    ): Single<Either<Authenticator.SignInException, IncompleteUser>> {

        return Single.create<Either<Authenticator.SignInException, IncompleteUser>> { emitter ->

            val successListener = { result: AuthResult ->
                emitter.onSuccess(result.user!!.toIncompleteUser().right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(when (throwable) {

                    is FirebaseAuthInvalidUserException ->
                        Authenticator.SignInException.AccountDoesNotExistOrHasBeenDisabledException

                    is FirebaseAuthInvalidCredentialsException ->
                        Authenticator.SignInException.WrongPasswordException

                    else -> Authenticator.SignInException.UnknownException(throwable)

                }.left())
            }

            auth.get().signInWithEmailAndPassword(email.value, password.value)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun signUp(
            credentials: UserCredentials
    ): Single<Either<Authenticator.SignUpException, IncompleteUser>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                Authenticator.SignUpException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            createSignUpSingle(credentials)
                        } else {
                            Single.just(
                                    Authenticator.SignUpException.NoInternetConnectionException.left()
                            )
                        }
                    })

                }
    }

    private fun createSignUpSingle(
            credentials: UserCredentials
    ): Single<Either<Authenticator.SignUpException, IncompleteUser>> {

        return Single.create<Either<Authenticator.SignUpException, IncompleteUser>> { emitter ->

            val successListener = { result: AuthResult ->
                emitter.onSuccess(result.user!!.toIncompleteUser().right())
            }

            val failureListener = failureListener@{ throwable: Throwable ->

                emitter.onSuccess(when (throwable) {

                    is FirebaseAuthWeakPasswordException ->
                        Authenticator.SignUpException.WeakPasswordException

                    is FirebaseAuthInvalidCredentialsException ->
                        Authenticator.SignUpException.MalformedEmailException

                    is FirebaseAuthUserCollisionException ->
                        Authenticator.SignUpException.EmailAlreadyInUseException(credentials.email)

                    else -> Authenticator.SignUpException.UnknownException(throwable)

                }.left())
            }

            auth.get().createUserWithEmailAndPassword(credentials.email.value, credentials.password.value)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun signInWithGoogle(): Single<Either<Authenticator.SignInWithGoogleException, IncompleteUser>> {

        fun SignInWithCredentialException.map() = when (this) {

            SignInWithCredentialException.NoResponseException ->
                Authenticator.SignInWithGoogleException.NoResponseException

            SignInWithCredentialException.AccountHasBeenDisabledException ->
                Authenticator.SignInWithGoogleException.AccountHasBeenDisabledException

            SignInWithCredentialException.MalformedOrExpiredCredentialException ->
                Authenticator.SignInWithGoogleException.MalformedOrExpiredCredentialException

            SignInWithCredentialException.EmailAlreadyInUseException ->
                Authenticator.SignInWithGoogleException.EmailAlreadyInUseException

            is SignInWithCredentialException.UnknownException ->
                Authenticator.SignInWithGoogleException.UnknownException(this.origin, this.providerId)
        }

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                Authenticator.SignInWithGoogleException.UnknownException(
                        this.origin,
                        GoogleAuthProvider.PROVIDER_ID
                )
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            createSignInWithGoogle()
                                    .flatMap(this::getGoogleAuthCredentials)
                                    .flatMap(this::signInWithCredentials)
                                    .map { userEither ->
                                        userEither.mapLeft(SignInWithCredentialException::map)
                                    }
                        } else {
                            Single.just(
                                    Authenticator.SignInWithGoogleException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }
    }

    private fun createSignInWithGoogle(): Single<Option<GoogleSignInAccount>> {
        return Single.create<Option<GoogleSignInAccount>> { emitter ->

            emitter.setCancellable { AuthenticatorBus.signInCancellation.accept(Unit) }

            emitter.onSuccess(GoogleSignIn.getLastSignedInAccount(appCtx).toOption())

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun getGoogleAuthCredentials(
            accountOptional: Option<GoogleSignInAccount>
    ): Single<Either<AuthActivityFactory.Exception, AuthCredential>> {
        return accountOptional.fold(ifEmpty = {
            getAuthCredentials(AuthActivityFactory::signInWithGoogle)
        }, ifSome = { googleSignInAccount ->
            if (googleSignInAccount.isExpired) {
                getAuthCredentials(AuthActivityFactory::signInWithGoogle)
            } else {
                Single.just(GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null).right())
            }
        })
    }

    override fun signInWithFacebook(): Single<Either<Authenticator.SignInWithFacebookException, IncompleteUser>> {

        fun SignInWithCredentialException.map() = when (this) {

            SignInWithCredentialException.NoResponseException ->
                Authenticator.SignInWithFacebookException.NoResponseException

            SignInWithCredentialException.AccountHasBeenDisabledException ->
                Authenticator.SignInWithFacebookException.AccountHasBeenDisabledException

            SignInWithCredentialException.MalformedOrExpiredCredentialException ->
                Authenticator.SignInWithFacebookException.MalformedOrExpiredCredentialException

            SignInWithCredentialException.EmailAlreadyInUseException ->
                Authenticator.SignInWithFacebookException.EmailAlreadyInUseException

            is SignInWithCredentialException.UnknownException ->
                Authenticator.SignInWithFacebookException.UnknownException(this.origin, this.providerId)
        }

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                Authenticator.SignInWithFacebookException.UnknownException(
                        this.origin,
                        FacebookAuthProvider.PROVIDER_ID
                )
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            createSignInWithFacebookSingle()
                                    .flatMap(this::getFacebookAuthCredentials)
                                    .flatMap(this::signInWithCredentials)
                                    .map { userEither ->
                                        userEither.mapLeft(SignInWithCredentialException::map)
                                    }
                        } else {
                            Single.just(
                                    Authenticator.SignInWithFacebookException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }
    }

    private fun createSignInWithFacebookSingle(): Single<Option<AccessToken>> {

        return Single.create<Option<AccessToken>> { emitter ->

            emitter.setCancellable { AuthenticatorBus.signInCancellation.accept(Unit) }

            emitter.onSuccess(AccessToken.getCurrentAccessToken().toOption())

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun getFacebookAuthCredentials(
            accessTokenOptional: Option<AccessToken>
    ): Single<Either<AuthActivityFactory.Exception, AuthCredential>> {
        return accessTokenOptional.fold(ifEmpty = {
            getAuthCredentials(AuthActivityFactory::signInWithFacebook)
        }, ifSome = { accessToken ->
            if (accessToken.isExpired) {
                getAuthCredentials(AuthActivityFactory::signInWithFacebook)
            } else {
                Single.just(FacebookAuthProvider.getCredential(accessToken.token).right())
            }
        })
    }

    override fun signInWithTwitter(): Single<Either<Authenticator.SignInWithTwitterException, IncompleteUser>> {

        fun SignInWithCredentialException.map() = when (this) {

            SignInWithCredentialException.NoResponseException ->
                Authenticator.SignInWithTwitterException.NoResponseException

            SignInWithCredentialException.AccountHasBeenDisabledException ->
                Authenticator.SignInWithTwitterException.AccountHasBeenDisabledException

            SignInWithCredentialException.MalformedOrExpiredCredentialException ->
                Authenticator.SignInWithTwitterException.MalformedOrExpiredCredentialException

            SignInWithCredentialException.EmailAlreadyInUseException ->
                Authenticator.SignInWithTwitterException.EmailAlreadyInUseException

            is SignInWithCredentialException.UnknownException ->
                Authenticator.SignInWithTwitterException.UnknownException(this.origin, this.providerId)
        }

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                Authenticator.SignInWithTwitterException.UnknownException(
                        this.origin,
                        TwitterAuthProvider.PROVIDER_ID
                )
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            createSignInWithTwitterSingle()
                                    .flatMap(this::getTwitterAuthCredentials)
                                    .flatMap(this::signInWithCredentials)
                                    .map { userEither ->
                                        userEither.mapLeft(SignInWithCredentialException::map)
                                    }
                        } else {
                            Single.just(
                                    Authenticator.SignInWithTwitterException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }
    }

    private fun createSignInWithTwitterSingle(): Single<Option<TwitterAuthToken>> {
        return Single.create<Option<TwitterAuthToken>> { emitter ->

            emitter.setCancellable { AuthenticatorBus.signInCancellation.accept(Unit) }

            emitter.onSuccess(TwitterCore.getInstance().sessionManager.activeSession?.authToken.toOption())

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun getTwitterAuthCredentials(
            authTokenOptional: Option<TwitterAuthToken>
    ): Single<Either<AuthActivityFactory.Exception, AuthCredential>> {
        return authTokenOptional.fold(ifEmpty = {
            getAuthCredentials(AuthActivityFactory::signInWithTwitter)
        }, ifSome = { twitterAuthToken ->
            if (twitterAuthToken.isExpired) {
                getAuthCredentials(AuthActivityFactory::signInWithTwitter)
            } else {
                Single.just(TwitterAuthProvider.getCredential(
                        twitterAuthToken.token,
                        twitterAuthToken.secret
                ).right())
            }
        })
    }

    private fun getAuthCredentials(
            createIntent: (AuthActivityFactory) -> Intent
    ): Single<Either<AuthActivityFactory.Exception, AuthCredential>> {
        appCtx.startActivity(createIntent(AuthSignInActivity.Companion))
        return AuthenticatorBus.signInCompletion
                .take(1)
                .single(AuthActivityFactory.Exception.NoResponseException.left())
    }

    private fun signInWithCredentials(
            credentialEither: Either<AuthActivityFactory.Exception, AuthCredential>
    ): Single<Either<SignInWithCredentialException, IncompleteUser>> {

        return Single.create<Either<SignInWithCredentialException, IncompleteUser>> { emitter ->

            credentialEither.fold(ifLeft = { e ->

                Single.just(when (e) {
                    AuthActivityFactory.Exception.NoResponseException ->
                        SignInWithCredentialException.NoResponseException
                    is AuthActivityFactory.Exception.UnknownException ->
                        SignInWithCredentialException.UnknownException(e.origin, e.providerId)
                }.left())

            }, ifRight = { credential ->

                val successListener = { result: AuthResult ->
                    emitter.onSuccess(result.user!!.toIncompleteUser().right())
                }

                val failureListener = failureListener@{ throwable: Throwable ->

                    emitter.onSuccess(when (throwable) {

                        is FirebaseAuthInvalidUserException ->
                            SignInWithCredentialException.AccountHasBeenDisabledException

                        is FirebaseAuthInvalidCredentialsException ->
                            SignInWithCredentialException.MalformedOrExpiredCredentialException

                        is FirebaseAuthUserCollisionException ->
                            SignInWithCredentialException.EmailAlreadyInUseException

                        else -> SignInWithCredentialException.UnknownException(throwable, credential.signInMethod)

                    }.left())
                }

                auth.get().signInWithCredential(credential)
                        .addOnSuccessListener(successListener)
                        .addOnFailureListener(failureListener)
            })

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun recoverFromUserCollisionIfNecessary(
            either: Either<Throwable, IncompleteUser>
    ): Single<Either<Throwable, IncompleteUser>> {
        return either.fold(ifLeft = { throwable ->
            if (throwable is AuthUserCollisionException) {
                signInWithCredentials(throwable.origin.updatedCredential!!.right())
                        .flatMap {
                            linkCurrentUserWithCredential(throwable.attemptedCredential)
                                    .map {
                                        it.map(FirebaseUser::toIncompleteUser)
                                    }
                        }
            } else {
                Single.just(throwable.left())
            }
        }, ifRight = {
            Single.just(it.right())
        })
    }

    private fun linkCurrentUserWithCredential(
            credential: AuthCredential
    ): Single<Either<Throwable, FirebaseUser>> {

        return Single.create<Either<Throwable, FirebaseUser>> { emitter ->

            val successListener = { result: AuthResult ->
                emitter.onSuccess(result.user!!.right())
            }

            val failureListener = { e: Exception ->
                emitter.onSuccess(e.left())
            }

            //TODO: convert to ? after you make sure it works
            auth.get().currentUser!!.linkWithCredential(credential)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun sendPasswordResetEmail(
            email: Email
    ): Single<Either<Authenticator.SendPasswordResetEmailException, Unit>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                Authenticator.SendPasswordResetEmailException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            createSendPasswordResetEmailSingle(email)
                        } else {
                            Single.just(
                                    Authenticator.SendPasswordResetEmailException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }
    }

    private fun createSendPasswordResetEmailSingle(
            email: Email
    ): Single<Either<Authenticator.SendPasswordResetEmailException, Unit>> {

        return Single.create<Either<Authenticator.SendPasswordResetEmailException, Unit>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(when (throwable) {

                    is FirebaseAuthInvalidUserException ->
                        Authenticator.SendPasswordResetEmailException.NonExistentEmailException(email)

                    else ->
                        Authenticator.SendPasswordResetEmailException.UnknownException(throwable)

                }.left())
            }

            auth.get().sendPasswordResetEmail(email.value)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun signOut(): Single<Either<Authenticator.SignOutException, UserId?>> {

        fun ConnectivityManager.IsInternetConnectedException.map() = when (this) {
            is ConnectivityManager.IsInternetConnectedException.UnknownException ->
                Authenticator.SignOutException.UnknownException(this.origin)
        }

        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnectedEither ->
                    isInternetConnectedEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { isInternetConnected ->
                        if (isInternetConnected) {
                            signOutFromFirebaseAuth()
                                    .andThen(signOutFromGoogle())
                        } else {
                            Single.just(
                                    Authenticator.SignOutException.NoInternetConnectionException.left()
                            )
                        }
                    })
                }.flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        signOutFromFacebook()
                                .andThen(signOutFromTwitter())
                                .andThen(Single.just(auth.get().currentUser?.uid?.let(::UserId).right()))
                    })
                }
    }

    private fun signOutFromFirebaseAuth(): Completable {
        return Completable.fromAction(auth.get()::signOut)
    }

    private fun signOutFromGoogle(): Single<Either<Authenticator.SignOutException, Unit>> {

        return Single.create<Either<Authenticator.SignOutException, Unit>> { emitter ->

            val successListener = { _: Void? ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(Authenticator.SignOutException.UnknownException(throwable).left())
            }

            GoogleSignIn.getClient(
                    appCtx,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut()
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    private fun signOutFromFacebook(): Completable {
        return Completable.fromAction(LoginManager.getInstance()::logOut)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    private fun signOutFromTwitter(): Completable {
        return Completable.fromAction(TwitterCore.getInstance().sessionManager::clearActiveSession)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    sealed class SignInWithCredentialException {
        object AccountHasBeenDisabledException : SignInWithCredentialException()
        object MalformedOrExpiredCredentialException : SignInWithCredentialException()
        object EmailAlreadyInUseException : SignInWithCredentialException()
        object NoResponseException : SignInWithCredentialException()
        data class UnknownException(val origin: Throwable, val providerId: String) : SignInWithCredentialException()
    }
}

interface AuthActivityFactory {

    fun signInWithGoogle(): Intent
    fun signInWithFacebook(): Intent
    fun signInWithTwitter(): Intent

    sealed class Exception {
        object NoResponseException : Exception()
        data class UnknownException(val origin: Throwable, val providerId: String) : Exception()
    }
}

private fun FirebaseUser.toIncompleteUser(): IncompleteUser {

    val id = UserId(this.uid)

    val email = this.email
            ?.takeUnless { it.isBlank() }
            ?.let(Email.Companion::of)
            ?.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }

    val displayName = this.displayName
            ?.takeUnless { it.isBlank() }
            ?.let(DisplayName.Companion::of)
            ?.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }

    val phoneNumber = this.phoneNumber
            ?.takeUnless { it.isBlank() }
            ?.let { PhoneNumber.of(it) }
            ?.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }

    val pictureUrl = this.photoUrl?.toString()
            ?.takeUnless { it.isBlank() }
            ?.let(Url.Companion::of)
            ?.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }

    return IncompleteUser.of(
            id,
            email,
            displayName,
            phoneNumber,
            pictureUrl
    )
}
