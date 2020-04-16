package dev.ahmedmourad.sherlock.auth.authenticator

import android.content.Intent
import arrow.core.*
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterCore
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.authenticator.activity.AuthSignInActivity
import dev.ahmedmourad.sherlock.auth.authenticator.bus.AuthenticatorBus
import dev.ahmedmourad.sherlock.auth.dagger.InternalApi
import dev.ahmedmourad.sherlock.auth.manager.ObserveUserAuthState
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.domain.exceptions.*
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

@Reusable
internal class ObserveUserAuthStateImpl @Inject constructor(
        @InternalApi private val auth: Lazy<FirebaseAuth>
) : ObserveUserAuthState {

    override fun invoke(): Flowable<Boolean> {
        return createObserveUserAuthState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    private fun createObserveUserAuthState(): Flowable<Boolean> {
        return Flowable.create<Boolean>({ emitter ->

            val authStateListener = { firebaseAuth: FirebaseAuth ->
                emitter.onNext(firebaseAuth.currentUser != null)
            }

            auth.get().addAuthStateListener(authStateListener)

            emitter.setCancellable { auth.get().removeAuthStateListener(authStateListener) }

        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }
}

@Reusable
internal class FirebaseAuthenticator @Inject constructor(
        @InternalApi private val auth: Lazy<FirebaseAuth>,
        @InternalApi private val observeUserAuthState: ObserveUserAuthState,
        private val connectivityManager: Lazy<ConnectivityManager>
) : Authenticator {

    override fun getCurrentUser(): Flowable<Either<Throwable, IncompleteUser>> {
        return connectivityManager.get()
                .observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        observeUserAuthState().map(Boolean::right)
                    } else {
                        Flowable.just(NoInternetConnectionException().left())
                    }
                }.flatMap { isUserSignedInEither ->
                    isUserSignedInEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { isUserSignedIn ->
                        if (isUserSignedIn) {
                            createGetCurrentUserFlowable()
                        } else {
                            Flowable.just(NoSignedInUserException().left())
                        }
                    })
                }
    }

    private fun createGetCurrentUserFlowable(): Flowable<Either<Throwable, IncompleteUser>> {
        return Flowable.just<Either<Throwable, IncompleteUser>>(
                auth.get()
                        .currentUser
                        ?.toIncompleteUser()
                        ?.rightIfNotNull { NoSignedInUserException() }
        ).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun signIn(
            credentials: UserCredentials
    ): Single<Either<Throwable, IncompleteUser>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        createSignInSingle(credentials.email, credentials.password)
                    } else {
                        Single.just(NoInternetConnectionException().left())
                    }
                }
    }

    private fun createSignInSingle(
            email: Email,
            password: Password
    ): Single<Either<Throwable, IncompleteUser>> {

        return Single.create<Either<Throwable, IncompleteUser>> { emitter ->

            val successListener = { result: AuthResult ->

                val user = result.user

                if (user != null) {
                    emitter.onSuccess(user.toIncompleteUser().right())
                } else {
                    emitter.onSuccess(NoSignedInUserException().left())
                }
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(when (throwable) {

                    is FirebaseAuthInvalidUserException -> InvalidUserException(
                            "The user account corresponding to the email does not exist or has been disabled!"
                    )

                    is FirebaseAuthInvalidCredentialsException -> InvalidCredentialsException(
                            "Wrong password!"
                    )

                    else -> throwable

                }.left())
            }

            auth.get().signInWithEmailAndPassword(email.value, password.value)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun signUp(credentials: UserCredentials): Single<Either<Throwable, IncompleteUser>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        createSignUpSingle(credentials)
                    } else {
                        Single.just(NoInternetConnectionException().left())
                    }
                }
    }

    private fun createSignUpSingle(credentials: UserCredentials): Single<Either<Throwable, IncompleteUser>> {

        return Single.create<Either<Throwable, IncompleteUser>> { emitter ->

            val successListener = { result: AuthResult ->

                val createdUser = result.user

                if (createdUser != null) {
                    emitter.onSuccess(createdUser.toIncompleteUser().right())
                } else { // Wait, that's illegal
                    emitter.onSuccess(NoSignedInUserException().left())
                }
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(when (throwable) {

                    is FirebaseAuthWeakPasswordException -> WeakPasswordException(
                            "The password is not strong enough!"
                    )

                    is FirebaseAuthInvalidCredentialsException -> InvalidCredentialsException(
                            "The email address is malformed!"
                    )

                    is FirebaseAuthUserCollisionException -> UserCollisionException(
                            "There already exists an account with the given email address!"
                    )

                    else -> throwable

                }.left())
            }

            auth.get().createUserWithEmailAndPassword(credentials.email.value, credentials.password.value)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun signInWithGoogle(): Single<Either<Throwable, IncompleteUser>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        createSignInWithGoogleSingle()
                    } else {
                        Single.just(NoInternetConnectionException().left())
                    }
                }
    }

    private fun createSignInWithGoogleSingle(): Single<Either<Throwable, IncompleteUser>> {

        return Single.create<Option<GoogleSignInAccount>> { emitter ->

            emitter.setCancellable { AuthenticatorBus.signInCancellation.accept(Unit) }

            emitter.onSuccess(GoogleSignIn.getLastSignedInAccount(appCtx).toOption())

        }.flatMap(this::getGoogleAuthCredentials)
                .flatMap(this::signInWithCredentials)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    private fun getGoogleAuthCredentials(accountOptional: Option<GoogleSignInAccount>): Single<Either<Throwable, AuthCredential>> {
        return accountOptional.fold(ifEmpty = {
            getAuthCredentials { it.signInWithGoogle() }
        }, ifSome = { googleSignInAccount ->
            if (googleSignInAccount.isExpired)
                getAuthCredentials { it.signInWithGoogle() }
            else
                Single.just(GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null).right())
        })
    }

    override fun signInWithFacebook(): Single<Either<Throwable, IncompleteUser>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        createSignInWithFacebookSingle()
                    } else {
                        Single.just(NoInternetConnectionException().left())
                    }
                }
    }

    private fun createSignInWithFacebookSingle(): Single<Either<Throwable, IncompleteUser>> {

        return Single.create<Option<AccessToken>> { emitter ->

            emitter.setCancellable { AuthenticatorBus.signInCancellation.accept(Unit) }

            emitter.onSuccess(AccessToken.getCurrentAccessToken().toOption())

        }.flatMap(this::getFacebookAuthCredentials)
                .flatMap(this::signInWithCredentials)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    private fun getFacebookAuthCredentials(accessTokenOptional: Option<AccessToken>): Single<Either<Throwable, AuthCredential>> {
        return accessTokenOptional.fold(ifEmpty = {
            getAuthCredentials { it.signInWithFacebook() }
        }, ifSome = { accessToken ->
            if (accessToken.isExpired) {
                getAuthCredentials { it.signInWithFacebook() }
            } else {
                Single.just(FacebookAuthProvider.getCredential(accessToken.token).right())
            }
        })
    }

    override fun signInWithTwitter(): Single<Either<Throwable, IncompleteUser>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected) {
                        createSignInWithTwitterSingle()
                    } else {
                        Single.just(NoInternetConnectionException().left())
                    }
                }
    }

    private fun createSignInWithTwitterSingle(): Single<Either<Throwable, IncompleteUser>> {

        return Single.create<Option<TwitterAuthToken>> { emitter ->

            emitter.setCancellable { AuthenticatorBus.signInCancellation.accept(Unit) }

            emitter.onSuccess(TwitterCore.getInstance().sessionManager.activeSession.authToken.toOption())

        }.flatMap(this::getTwitterAuthCredentials)
                .flatMap(this::signInWithCredentials)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    private fun getTwitterAuthCredentials(authTokenOptional: Option<TwitterAuthToken>): Single<Either<Throwable, AuthCredential>> {
        return authTokenOptional.fold(ifEmpty = {
            getAuthCredentials { it.signInWithTwitter() }
        }, ifSome = { twitterAuthToken ->
            if (twitterAuthToken.isExpired) {
                getAuthCredentials { it.signInWithTwitter() }
            } else {
                Single.just(TwitterAuthProvider.getCredential(twitterAuthToken.token, twitterAuthToken.secret).right())
            }
        })
    }

    private fun getAuthCredentials(createIntent: (AuthActivityFactory) -> Intent): Single<Either<Throwable, AuthCredential>> {
        appCtx.startActivity(createIntent(AuthSignInActivity.Companion))
        return AuthenticatorBus.signInCompletion
                .buffer(1)
                .map(List<Either<Throwable, AuthCredential>>::first)
                .single(NoSignedInUserException().left())
    }

    private fun signInWithCredentials(
            credentialEither: Either<Throwable, AuthCredential>
    ): Single<Either<Throwable, IncompleteUser>> {

        return Single.create<Either<Throwable, IncompleteUser>> { emitter ->

            credentialEither.fold(ifLeft = {

                Single.just(it.left())

            }, ifRight = {

                val successListener = { result: AuthResult ->

                    val user = result.user

                    if (user != null)
                        emitter.onSuccess(user.toIncompleteUser().right())
                    else
                        emitter.onSuccess(NoSignedInUserException().left())
                }

                val failureListener = { throwable: Throwable ->
                    emitter.onSuccess(when (throwable) {

                        is FirebaseAuthInvalidUserException -> InvalidUserException(
                                "The user account you are trying to sign in to has been disabled!"
                        )

                        is FirebaseAuthInvalidCredentialsException -> InvalidCredentialsException(
                                "The credential is malformed or has expired!"
                        )

                        //TODO: can be handled, check docs
                        is FirebaseAuthUserCollisionException -> UserCollisionException(
                                "There already exists an account with the email address asserted by the credential!"
                        )

                        else -> throwable

                    }.left())
                }

                auth.get().signInWithCredential(it)
                        .addOnSuccessListener(successListener)
                        .addOnFailureListener(failureListener)
            })

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun sendPasswordResetEmail(email: Email): Single<Either<Throwable, Unit>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected)
                        createSendPasswordResetEmailSingle(email)
                    else
                        Single.just(NoInternetConnectionException().left())
                }
    }

    private fun createSendPasswordResetEmailSingle(email: Email): Single<Either<Throwable, Unit>> {

        return Single.create<Either<Throwable, Unit>> { emitter ->

            val successListener = { _: Void ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(when (throwable) {

                    is FirebaseAuthInvalidUserException -> InvalidUserException(
                            "There is no user corresponding to the given email address!"
                    )

                    else -> throwable

                }.left())
            }

            auth.get().sendPasswordResetEmail(email.value)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener)

        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
    }

    override fun signOut(): Single<Either<Throwable, UserId?>> {
        return connectivityManager.get()
                .isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { isInternetConnected ->
                    if (isInternetConnected)
                        signOutFromFirebaseAuth()
                                .andThen(signOutFromGoogle())
                    else
                        Single.just(NoInternetConnectionException().left())
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

    private fun signOutFromGoogle(): Single<Either<Throwable, Unit>> {

        return Single.create<Either<Throwable, Unit>> { emitter ->

            val successListener = { _: Void ->
                emitter.onSuccess(Unit.right())
            }

            val failureListener = { throwable: Throwable ->
                emitter.onSuccess(throwable.left())
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
}

interface AuthActivityFactory {
    fun signInWithGoogle(): Intent
    fun signInWithFacebook(): Intent
    fun signInWithTwitter(): Intent
}

private fun FirebaseUser.toIncompleteUser(): IncompleteUser {

    val id = UserId(this.uid)

    val email = this.email
            ?.let(Email.Companion::of)
            ?.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }

    val displayName = this.displayName
            ?.let(DisplayName.Companion::of)
            ?.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }

    val phoneNumber = this.phoneNumber
            ?.let { PhoneNumber.of(it) }
            ?.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }

    val pictureUrl = this.photoUrl?.toString()
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
    ).getOrHandle {
        Timber.error(ModelCreationException(it.toString()), it::toString)
        null
    }!!
}
