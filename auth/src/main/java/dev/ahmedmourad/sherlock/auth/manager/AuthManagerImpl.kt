package dev.ahmedmourad.sherlock.auth.manager

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.auth.di.InternalApi
import dev.ahmedmourad.sherlock.auth.manager.dependencies.Authenticator
import dev.ahmedmourad.sherlock.auth.manager.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.auth.manager.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.auth.manager.dependencies.UserAuthStateObservable
import dev.ahmedmourad.sherlock.auth.mapper.toRemoteSignUpUser
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Reusable
internal class AuthManagerImpl @Inject constructor(
        @InternalApi private val authenticator: Lazy<Authenticator>,
        @InternalApi private val remoteRepository: Lazy<RemoteRepository>,
        @InternalApi private val imageRepository: Lazy<ImageRepository>,
        @InternalApi private val userAuthStateObservable: Lazy<UserAuthStateObservable>
) : AuthManager {

    override fun observeUserAuthState(): Flowable<Either<AuthManager.ObserveUserAuthStateException, Boolean>> {
        return userAuthStateObservable.get()
                .observeUserAuthState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map<Either<AuthManager.ObserveUserAuthStateException, Boolean>> {
                    it.right()
                }.onErrorReturn {
                    AuthManager.ObserveUserAuthStateException.UnknownException(it).left()
                }
    }

    override fun observeSignedInUser():
            Flowable<Either<AuthManager.ObserveSignedInUserException, Either<IncompleteUser, SignedInUser>?>> {

        fun RemoteRepository.FindSignedInUserException.map() = when (this) {

            RemoteRepository.FindSignedInUserException.NoInternetConnectionException ->
                AuthManager.ObserveSignedInUserException.NoInternetConnectionException

            RemoteRepository.FindSignedInUserException.NoSignedInUserException ->
                null

            is RemoteRepository.FindSignedInUserException.InternalException ->
                AuthManager.ObserveSignedInUserException.InternalException(this.origin)

            is RemoteRepository.FindSignedInUserException.UnknownException ->
                AuthManager.ObserveSignedInUserException.UnknownException(this.origin)
        }

        fun RemoteRepository.UpdateUserLastLoginDateException.map() = when (this) {

            RemoteRepository.UpdateUserLastLoginDateException.NoInternetConnectionException ->
                AuthManager.ObserveSignedInUserException.NoInternetConnectionException

            RemoteRepository.UpdateUserLastLoginDateException.NoSignedInUserException ->
                null

            is RemoteRepository.UpdateUserLastLoginDateException.UnknownException ->
                AuthManager.ObserveSignedInUserException.UnknownException(this.origin)
        }

        return authenticator.get()
                .getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { incompleteUserOption ->
                    incompleteUserOption.fold(ifEmpty = {
                        Flowable.just(null.right())
                    }, ifSome = { incompleteUser ->
                        remoteRepository.get()
                                .findSignedInUser(incompleteUser.id)
                                .map { userEither ->
                                    userEither.bimap(
                                            leftOperation =
                                            RemoteRepository.FindSignedInUserException::map,
                                            rightOperation = { user ->
                                                user?.right() ?: incompleteUser.left()
                                            }
                                    )
                                }
                    })
                }.flatMap { either ->
                    either.fold(ifLeft = {
                        Flowable.just(it?.left() ?: null.right())
                    }, ifRight = { userEither ->
                        userEither?.fold(ifLeft = { incompleteUser ->
                            Flowable.just(incompleteUser.left().right())
                        }, ifRight = { signedInUser ->
                            remoteRepository.get()
                                    .updateUserLastLoginDate(signedInUser.id)
                                    .flatMap { either ->
                                        either.fold(ifLeft = { e ->
                                            Single.just(e.map()?.left() ?: null.right())
                                        }, ifRight = {
                                            Single.just(signedInUser.right().right())
                                        })
                                    }.toFlowable()
                        })
                    })
                }.onErrorReturn {
                    AuthManager.ObserveSignedInUserException.UnknownException(it).left()
                }
    }

    override fun signIn(
            credentials: UserCredentials
    ): Single<Either<AuthManager.SignInException, Either<IncompleteUser, SignedInUser>>> {

        fun Authenticator.SignInException.map() = when (this) {

            Authenticator.SignInException.AccountDoesNotExistOrHasBeenDisabledException ->
                AuthManager.SignInException.AccountDoesNotExistOrHasBeenDisabledException

            Authenticator.SignInException.WrongPasswordException ->
                AuthManager.SignInException.WrongPasswordException

            Authenticator.SignInException.NoInternetConnectionException ->
                AuthManager.SignInException.NoInternetConnectionException

            is Authenticator.SignInException.UnknownException ->
                AuthManager.SignInException.UnknownException(this.origin)
        }

        fun RemoteRepository.FindSignedInUserException.map() = when (this) {

            RemoteRepository.FindSignedInUserException.NoInternetConnectionException ->
                AuthManager.SignInException.NoInternetConnectionException

            RemoteRepository.FindSignedInUserException.NoSignedInUserException ->
                AuthManager.SignInException.InternalException(IllegalStateException(
                        RemoteRepository.FindSignedInUserException.NoSignedInUserException.toString()
                ))

            is RemoteRepository.FindSignedInUserException.InternalException ->
                AuthManager.SignInException.InternalException(this.origin)

            is RemoteRepository.FindSignedInUserException.UnknownException ->
                AuthManager.SignInException.UnknownException(this.origin)
        }

        return authenticator.get()
                .signIn(credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { incompleteUser ->
                        remoteRepository.get()
                                .findSignedInUser(incompleteUser.id)
                                .map { userEither ->
                                    userEither.bimap(
                                            leftOperation =
                                            RemoteRepository.FindSignedInUserException::map,
                                            rightOperation = { user ->
                                                user?.right() ?: incompleteUser.left()
                                            })
                                }.firstOrError()
                    })
                }.onErrorReturn {
                    AuthManager.SignInException.UnknownException(it).left()
                }
    }

    override fun signUp(user: SignUpUser): Single<Either<AuthManager.SignUpException, SignedInUser>> {

        fun Authenticator.SignUpException.map() = when (this) {

            Authenticator.SignUpException.WeakPasswordException ->
                AuthManager.SignUpException.WeakPasswordException

            Authenticator.SignUpException.MalformedEmailException ->
                AuthManager.SignUpException.MalformedEmailException

            is Authenticator.SignUpException.EmailAlreadyInUseException ->
                AuthManager.SignUpException.EmailAlreadyInUseException(this.email)

            Authenticator.SignUpException.NoInternetConnectionException ->
                AuthManager.SignUpException.NoInternetConnectionException

            is Authenticator.SignUpException.UnknownException ->
                AuthManager.SignUpException.UnknownException(this.origin)
        }

        fun ImageRepository.StoreUserPictureException.map() = when (this) {

            ImageRepository.StoreUserPictureException.NoInternetConnectionException ->
                AuthManager.SignUpException.NoInternetConnectionException

            ImageRepository.StoreUserPictureException.NoSignedInUserException ->
                AuthManager.SignUpException.InternalException(IllegalStateException(
                        RemoteRepository.FindSignedInUserException.NoSignedInUserException.toString()
                ))

            is ImageRepository.StoreUserPictureException.InternalException ->
                AuthManager.SignUpException.InternalException(this.origin)

            is ImageRepository.StoreUserPictureException.UnknownException ->
                AuthManager.SignUpException.UnknownException(this.origin)
        }

        fun RemoteRepository.StoreSignUpUserException.map() = when (this) {

            RemoteRepository.StoreSignUpUserException.NoInternetConnectionException ->
                AuthManager.SignUpException.NoInternetConnectionException

            RemoteRepository.StoreSignUpUserException.NoSignedInUserException ->
                AuthManager.SignUpException.InternalException(IllegalStateException(
                        RemoteRepository.FindSignedInUserException.NoSignedInUserException.toString()
                ))

            is RemoteRepository.StoreSignUpUserException.UnknownException ->
                AuthManager.SignUpException.UnknownException(this.origin)
        }

        return authenticator.get()
                .signUp(user.credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { incompleteUserEither ->
                    incompleteUserEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { incompleteUser ->
                        imageRepository.get()
                                .storeUserPicture(incompleteUser.id, user.picture)
                                .map { urlEither ->
                                    urlEither.bimap(
                                            leftOperation =
                                            ImageRepository.StoreUserPictureException::map,
                                            rightOperation = {
                                                incompleteUser.id to it
                                            })
                                }
                    })
                }.flatMap { idAndUrlEither ->
                    idAndUrlEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { (id, url) ->
                        remoteRepository.get()
                                .storeSignUpUser(user.toRemoteSignUpUser(id, url))
                                .map { either ->
                                    either.mapLeft(RemoteRepository.StoreSignUpUserException::map)
                                }
                    })
                }.onErrorReturn {
                    AuthManager.SignUpException.UnknownException(it).left()
                }
    }

    override fun completeSignUp(
            completedUser: CompletedUser
    ): Single<Either<AuthManager.CompleteSignUpException, SignedInUser>> {

        fun ImageRepository.StoreUserPictureException.map() = when (this) {

            ImageRepository.StoreUserPictureException.NoInternetConnectionException ->
                AuthManager.CompleteSignUpException.NoInternetConnectionException

            ImageRepository.StoreUserPictureException.NoSignedInUserException ->
                AuthManager.CompleteSignUpException.NoSignedInUserException

            is ImageRepository.StoreUserPictureException.InternalException ->
                AuthManager.CompleteSignUpException.InternalException(this.origin)

            is ImageRepository.StoreUserPictureException.UnknownException ->
                AuthManager.CompleteSignUpException.UnknownException(this.origin)
        }

        fun RemoteRepository.StoreSignUpUserException.map() = when (this) {

            RemoteRepository.StoreSignUpUserException.NoInternetConnectionException ->
                AuthManager.CompleteSignUpException.NoInternetConnectionException

            RemoteRepository.StoreSignUpUserException.NoSignedInUserException ->
                AuthManager.CompleteSignUpException.NoSignedInUserException

            is RemoteRepository.StoreSignUpUserException.UnknownException ->
                AuthManager.CompleteSignUpException.UnknownException(this.origin)
        }

        return imageRepository.get()
                .storeUserPicture(completedUser.id, completedUser.picture)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { urlEither ->
                    urlEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { url ->
                        remoteRepository.get()
                                .storeSignUpUser(completedUser.toRemoteSignUpUser(url))
                                .map { either ->
                                    either.mapLeft(RemoteRepository.StoreSignUpUserException::map)
                                }
                    })
                }.onErrorReturn {
                    AuthManager.CompleteSignUpException.UnknownException(it).left()
                }
    }

    override fun signInWithGoogle():
            Single<Either<AuthManager.SignInWithGoogleException, Either<IncompleteUser, SignedInUser>>> {

        fun Authenticator.SignInWithGoogleException.map() = when (this) {

            Authenticator.SignInWithGoogleException.AccountHasBeenDisabledException ->
                AuthManager.SignInWithGoogleException.AccountHasBeenDisabledException

            Authenticator.SignInWithGoogleException.MalformedOrExpiredCredentialException ->
                AuthManager.SignInWithGoogleException.MalformedOrExpiredCredentialException

            Authenticator.SignInWithGoogleException.EmailAlreadyInUseException ->
                AuthManager.SignInWithGoogleException.EmailAlreadyInUseException

            Authenticator.SignInWithGoogleException.NoResponseException ->
                AuthManager.SignInWithGoogleException.NoResponseException

            Authenticator.SignInWithGoogleException.NoInternetConnectionException ->
                AuthManager.SignInWithGoogleException.NoInternetConnectionException

            is Authenticator.SignInWithGoogleException.UnknownException ->
                AuthManager.SignInWithGoogleException.UnknownException(this.origin, this.providerId)
        }

        fun RemoteRepository.FindSignedInUserException.map() = when (this) {

            RemoteRepository.FindSignedInUserException.NoInternetConnectionException ->
                AuthManager.SignInWithGoogleException.NoInternetConnectionException

            RemoteRepository.FindSignedInUserException.NoSignedInUserException ->
                AuthManager.SignInWithGoogleException.InternalException(IllegalStateException(
                        RemoteRepository.FindSignedInUserException.NoSignedInUserException.toString()
                ), GoogleAuthProvider.PROVIDER_ID)

            is RemoteRepository.FindSignedInUserException.InternalException ->
                AuthManager.SignInWithGoogleException.InternalException(
                        this.origin,
                        GoogleAuthProvider.PROVIDER_ID
                )

            is RemoteRepository.FindSignedInUserException.UnknownException ->
                AuthManager.SignInWithGoogleException.UnknownException(
                        this.origin,
                        GoogleAuthProvider.PROVIDER_ID
                )
        }

        return authenticator.get()
                .signInWithGoogle()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { incompleteUser ->
                        remoteRepository.get()
                                .findSignedInUser(incompleteUser.id)
                                .map { userEither ->
                                    userEither.bimap(
                                            leftOperation =
                                            RemoteRepository.FindSignedInUserException::map,
                                            rightOperation = { user ->
                                                user?.right() ?: incompleteUser.left()
                                            })
                                }.firstOrError()
                    })
                }.onErrorReturn {
                    AuthManager.SignInWithGoogleException.UnknownException(
                            it,
                            GoogleAuthProvider.PROVIDER_ID
                    ).left()
                }
    }

    override fun signInWithFacebook():
            Single<Either<AuthManager.SignInWithFacebookException, Either<IncompleteUser, SignedInUser>>> {

        fun Authenticator.SignInWithFacebookException.map() = when (this) {

            Authenticator.SignInWithFacebookException.AccountHasBeenDisabledException ->
                AuthManager.SignInWithFacebookException.AccountHasBeenDisabledException

            Authenticator.SignInWithFacebookException.MalformedOrExpiredCredentialException ->
                AuthManager.SignInWithFacebookException.MalformedOrExpiredCredentialException

            Authenticator.SignInWithFacebookException.EmailAlreadyInUseException ->
                AuthManager.SignInWithFacebookException.EmailAlreadyInUseException

            Authenticator.SignInWithFacebookException.NoResponseException ->
                AuthManager.SignInWithFacebookException.NoResponseException

            Authenticator.SignInWithFacebookException.NoInternetConnectionException ->
                AuthManager.SignInWithFacebookException.NoInternetConnectionException

            is Authenticator.SignInWithFacebookException.UnknownException ->
                AuthManager.SignInWithFacebookException.UnknownException(this.origin, this.providerId)
        }

        fun RemoteRepository.FindSignedInUserException.map() = when (this) {

            RemoteRepository.FindSignedInUserException.NoInternetConnectionException ->
                AuthManager.SignInWithFacebookException.NoInternetConnectionException

            RemoteRepository.FindSignedInUserException.NoSignedInUserException ->
                AuthManager.SignInWithFacebookException.InternalException(IllegalStateException(
                        RemoteRepository.FindSignedInUserException.NoSignedInUserException.toString()
                ), FacebookAuthProvider.PROVIDER_ID)

            is RemoteRepository.FindSignedInUserException.InternalException ->
                AuthManager.SignInWithFacebookException.InternalException(
                        this.origin,
                        FacebookAuthProvider.PROVIDER_ID
                )

            is RemoteRepository.FindSignedInUserException.UnknownException ->
                AuthManager.SignInWithFacebookException.UnknownException(
                        this.origin,
                        FacebookAuthProvider.PROVIDER_ID
                )
        }

        return authenticator.get()
                .signInWithFacebook()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { incompleteUser ->
                        remoteRepository.get()
                                .findSignedInUser(incompleteUser.id)
                                .map { userEither ->
                                    userEither.bimap(
                                            leftOperation =
                                            RemoteRepository.FindSignedInUserException::map,
                                            rightOperation = { user ->
                                                user?.right() ?: incompleteUser.left()
                                            })
                                }.firstOrError()
                    })
                }.onErrorReturn {
                    AuthManager.SignInWithFacebookException.UnknownException(
                            it,
                            FacebookAuthProvider.PROVIDER_ID
                    ).left()
                }
    }

    override fun signInWithTwitter():
            Single<Either<AuthManager.SignInWithTwitterException, Either<IncompleteUser, SignedInUser>>> {

        fun Authenticator.SignInWithTwitterException.map() = when (this) {

            Authenticator.SignInWithTwitterException.AccountHasBeenDisabledException ->
                AuthManager.SignInWithTwitterException.AccountHasBeenDisabledException

            Authenticator.SignInWithTwitterException.MalformedOrExpiredCredentialException ->
                AuthManager.SignInWithTwitterException.MalformedOrExpiredCredentialException

            Authenticator.SignInWithTwitterException.EmailAlreadyInUseException ->
                AuthManager.SignInWithTwitterException.EmailAlreadyInUseException

            Authenticator.SignInWithTwitterException.NoResponseException ->
                AuthManager.SignInWithTwitterException.NoResponseException

            Authenticator.SignInWithTwitterException.NoInternetConnectionException ->
                AuthManager.SignInWithTwitterException.NoInternetConnectionException

            is Authenticator.SignInWithTwitterException.UnknownException ->
                AuthManager.SignInWithTwitterException.UnknownException(this.origin, this.providerId)
        }

        fun RemoteRepository.FindSignedInUserException.map() = when (this) {

            RemoteRepository.FindSignedInUserException.NoInternetConnectionException ->
                AuthManager.SignInWithTwitterException.NoInternetConnectionException

            RemoteRepository.FindSignedInUserException.NoSignedInUserException ->
                AuthManager.SignInWithTwitterException.InternalException(IllegalStateException(
                        RemoteRepository.FindSignedInUserException.NoSignedInUserException.toString()
                ), TwitterAuthProvider.PROVIDER_ID)

            is RemoteRepository.FindSignedInUserException.InternalException ->
                AuthManager.SignInWithTwitterException.InternalException(
                        this.origin,
                        TwitterAuthProvider.PROVIDER_ID
                )

            is RemoteRepository.FindSignedInUserException.UnknownException ->
                AuthManager.SignInWithTwitterException.UnknownException(
                        this.origin,
                        TwitterAuthProvider.PROVIDER_ID
                )
        }

        return authenticator.get()
                .signInWithTwitter()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = { incompleteUser ->
                        remoteRepository.get()
                                .findSignedInUser(incompleteUser.id)
                                .map { userEither ->
                                    userEither.bimap(
                                            leftOperation =
                                            RemoteRepository.FindSignedInUserException::map,
                                            rightOperation = { user ->
                                                user?.right() ?: incompleteUser.left()
                                            })
                                }.firstOrError()
                    })
                }.onErrorReturn {
                    AuthManager.SignInWithTwitterException.UnknownException(
                            it,
                            TwitterAuthProvider.PROVIDER_ID
                    ).left()
                }
    }

    override fun sendPasswordResetEmail(
            email: Email
    ): Single<Either<AuthManager.SendPasswordResetEmailException, Unit>> {

        fun Authenticator.SendPasswordResetEmailException.map() = when (this) {

            is Authenticator.SendPasswordResetEmailException.NonExistentEmailException ->
                AuthManager.SendPasswordResetEmailException.NonExistentEmailException(this.email)

            Authenticator.SendPasswordResetEmailException.NoInternetConnectionException ->
                AuthManager.SendPasswordResetEmailException.NoInternetConnectionException

            is Authenticator.SendPasswordResetEmailException.UnknownException ->
                AuthManager.SendPasswordResetEmailException.UnknownException(this.origin)
        }

        return authenticator.get()
                .sendPasswordResetEmail(email)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { either ->
                    either.mapLeft(Authenticator.SendPasswordResetEmailException::map)
                }.onErrorReturn {
                    AuthManager.SendPasswordResetEmailException.UnknownException(it).left()
                }
    }

    override fun signOut(): Single<Either<AuthManager.SignOutException, Unit>> {

        fun Authenticator.SignOutException.map() = when (this) {

            Authenticator.SignOutException.NoInternetConnectionException ->
                AuthManager.SignOutException.NoInternetConnectionException

            is Authenticator.SignOutException.UnknownException ->
                AuthManager.SignOutException.UnknownException(this.origin)
        }

        fun RemoteRepository.UpdateUserLastLoginDateException.map() = when (this) {

            RemoteRepository.UpdateUserLastLoginDateException.NoInternetConnectionException ->
                AuthManager.SignOutException.NoInternetConnectionException

            RemoteRepository.UpdateUserLastLoginDateException.NoSignedInUserException ->
                AuthManager.SignOutException.InternalException(IllegalStateException(
                        RemoteRepository.UpdateUserLastLoginDateException.NoSignedInUserException.toString()
                ))

            is RemoteRepository.UpdateUserLastLoginDateException.UnknownException ->
                AuthManager.SignOutException.UnknownException(this.origin)
        }

        return authenticator.get()
                .getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .singleOrError()
                .flatMap { userOption ->
                    userOption.fold(ifEmpty = {
                        Single.just(Unit.right())
                    }, ifSome = { user ->
                        remoteRepository.get()
                                .updateUserLastLoginDate(user.id)
                                .map { either ->
                                    either.mapLeft(RemoteRepository.UpdateUserLastLoginDateException::map)
                                }
                    })
                }.flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        authenticator.get()
                                .signOut()
                                .map { either ->
                                    either.bimap(
                                            leftOperation =
                                            Authenticator.SignOutException::map,
                                            rightOperation = { Unit }
                                    )
                                }
                    })
                }.onErrorReturn {
                    AuthManager.SignOutException.UnknownException(it).left()
                }
    }
}
