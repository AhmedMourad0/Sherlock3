package dev.ahmedmourad.sherlock.auth.manager

import arrow.core.Either
import arrow.core.left
import arrow.core.right
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
        @InternalApi private val userAuthStateObservable: UserAuthStateObservable
) : AuthManager {

    override fun observeUserAuthState(): Flowable<Boolean> {
        return userAuthStateObservable.observeUserAuthState()
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
        return createSignInWithProvider(Authenticator::signInWithGoogle)
    }

    override fun signInWithFacebook(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return createSignInWithProvider(Authenticator::signInWithFacebook)
    }

    override fun signInWithTwitter(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return createSignInWithProvider(Authenticator::signInWithTwitter)
    }

    private fun createSignInWithProvider(
            signInWithProvider: Authenticator.() -> Single<Either<Throwable, IncompleteUser>>
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
