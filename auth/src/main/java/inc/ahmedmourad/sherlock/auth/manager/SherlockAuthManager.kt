package inc.ahmedmourad.sherlock.auth.manager

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dagger.Lazy
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthAuthenticator
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthImageRepository
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthRemoteRepository
import inc.ahmedmourad.sherlock.auth.mapper.toRemoteSignUpUser
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

internal typealias ObserveUserAuthState = () -> @JvmSuppressWildcards Flowable<Boolean>

internal class SherlockAuthManager(
        private val authenticator: Lazy<AuthAuthenticator>,
        private val remoteRepository: Lazy<AuthRemoteRepository>,
        private val imageRepository: Lazy<AuthImageRepository>
) : AuthManager {

    override fun observeUserAuthState(): Flowable<Boolean> {
        return authenticator.get()
                .observeUserAuthState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    override fun observeSignedInUser(): Flowable<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return authenticator.get()
                .getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { incompleteUserEither ->
                    incompleteUserEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { incompleteUser ->
                        remoteRepository.get()
                                .findSignedInUser(incompleteUser.id)
                                .map { userEither ->
                                    userEither.map { user ->
                                        user?.right() ?: incompleteUser.left()
                                    }
                                }
                    })
                }.flatMap { either ->
                    either.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { userEither ->
                        userEither.fold(ifLeft = { incompleteUser ->
                            Flowable.just(incompleteUser.left().right())
                        }, ifRight = { signedInUser ->
                            remoteRepository.get()
                                    .updateUserLastLoginDate(signedInUser.id)
                                    .map { signedInUser.right().right() }
                                    .toFlowable()
                        })
                    })
                }
    }

    override fun signIn(credentials: UserCredentials): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return authenticator.get()
                .signIn(credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { incompleteUser ->
                        remoteRepository.get()
                                .findSignedInUser(incompleteUser.id)
                                .map { userEither ->
                                    userEither.map { user ->
                                        user?.right() ?: incompleteUser.left()
                                    }
                                }.firstOrError()
                    })
                }
    }

    override fun signUp(user: SignUpUser): Single<Either<Throwable, SignedInUser>> {
        return authenticator.get()
                .signUp(user.credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { incompleteUserEither ->
                    incompleteUserEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { incompleteUser ->
                        imageRepository.get()
                                .storeUserPicture(incompleteUser.id, user.picture)
                                .map { urlEither ->
                                    urlEither.map {
                                        incompleteUser.id to it
                                    }
                                }
                    })
                }.flatMap { idAndUrlEither ->
                    idAndUrlEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { (id, url) ->
                        remoteRepository.get()
                                .storeSignUpUser(user.toRemoteSignUpUser(id, url))
                    })
                }
    }

    override fun completeSignUp(completedUser: CompletedUser): Single<Either<Throwable, SignedInUser>> {
        return imageRepository.get()
                .storeUserPicture(completedUser.id, completedUser.picture)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { urlEither ->
                    urlEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { url ->
                        remoteRepository.get()
                                .storeSignUpUser(completedUser.toRemoteSignUpUser(url))
                    })
                }
    }

    override fun signInWithGoogle(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return createSignInWithProvider(AuthAuthenticator::signInWithGoogle)
    }

    override fun signInWithFacebook(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return createSignInWithProvider(AuthAuthenticator::signInWithFacebook)
    }

    override fun signInWithTwitter(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return createSignInWithProvider(AuthAuthenticator::signInWithTwitter)
    }

    private fun createSignInWithProvider(
            signInWithProvider: AuthAuthenticator.() -> Single<Either<Throwable, IncompleteUser>>
    ): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return authenticator.get()
                .signInWithProvider()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { incompleteUser ->
                            remoteRepository.get()
                                    .findSignedInUser(incompleteUser.id)
                                    .map { userEither ->
                                        userEither.map { user ->
                                            user?.right() ?: incompleteUser.left()
                                        }
                                    }.firstOrError()
                    })
                }
    }

    override fun sendPasswordResetEmail(email: Email): Single<Either<Throwable, Unit>> {
        return authenticator.get()
                .sendPasswordResetEmail(email)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    override fun signOut(): Single<Either<Throwable, Unit>> {
        return authenticator.get()
                .signOut()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { userUidEither ->
                    userUidEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userUid ->
                        if (userUid == null) {
                            Single.just(Unit.right())
                        } else {
                            remoteRepository.get()
                                    .updateUserLastLoginDate(userUid)
                                    .map { Unit.right() }
                        }
                    })
                }
    }
}
