package dev.ahmedmourad.sherlock.auth.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.auth.manager.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.auth.model.RemoteSignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

internal class FakeRemoteRepository : RemoteRepository {

    private val fakeUsersDb = mutableListOf<Pair<SignedInUser, Long>>()

    var hasInternet = true
    var isUserSignedIn = true
    var triggerInternalException = false
    var triggerUnknownException = false

    override fun storeSignUpUser(
            user: RemoteSignUpUser
    ): Single<Either<RemoteRepository.StoreSignUpUserException, SignedInUser>> {
        return Single.defer {
            when {

                !hasInternet -> {
                    Single.just(RemoteRepository.StoreSignUpUserException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Single.just(RemoteRepository.StoreSignUpUserException.NoSignedInUserException.left())
                }

                triggerUnknownException -> {
                    Single.just(RemoteRepository.StoreSignUpUserException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    val retrieved = user.toSignedInUser(System.currentTimeMillis())
                    fakeUsersDb.add(retrieved to retrieved.timestamp)
                    Single.just(retrieved.right())
                }
            }
        }
    }

    override fun findSignedInUser(
            id: UserId
    ): Flowable<Either<RemoteRepository.FindSignedInUserException, SignedInUser?>> {
        return Flowable.defer {
            when {

                !hasInternet -> {
                    Flowable.just(RemoteRepository.FindSignedInUserException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Flowable.just(RemoteRepository.FindSignedInUserException.NoSignedInUserException.left())
                }

                triggerInternalException -> {
                    Flowable.just(RemoteRepository.FindSignedInUserException.InternalException(RuntimeException()).left())
                }

                triggerUnknownException -> {
                    Flowable.just(RemoteRepository.FindSignedInUserException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    Flowable.just(fakeUsersDb.firstOrNull {
                        it.first.id == id
                    }?.first.right())
                }
            }
        }
    }

    override fun findSimpleUsers(
            ids: Collection<UserId>
    ): Flowable<Either<RemoteRepository.FindSimpleUsersException, List<SimpleRetrievedUser>>> {
        return Flowable.defer {
            when {

                !hasInternet -> {
                    Flowable.just(RemoteRepository.FindSimpleUsersException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Flowable.just(RemoteRepository.FindSimpleUsersException.NoSignedInUserException.left())
                }

                triggerUnknownException -> {
                    Flowable.just(RemoteRepository.FindSimpleUsersException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    Flowable.just(fakeUsersDb.filter {
                        it.first.id in ids
                    }.map {
                        it.first.simplify()
                    }.right())
                }
            }
        }
    }

    override fun updateUserLastLoginDate(
            id: UserId
    ): Single<Either<RemoteRepository.UpdateUserLastLoginDateException, Unit>> {
        return Single.defer {
            when {

                !hasInternet -> {
                    Single.just(RemoteRepository.UpdateUserLastLoginDateException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Single.just(RemoteRepository.UpdateUserLastLoginDateException.NoSignedInUserException.left())
                }

                triggerUnknownException -> {
                    Single.just(RemoteRepository.UpdateUserLastLoginDateException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    Completable.fromAction {
                        fakeUsersDb.map {
                            if (it.first.id == id) {
                                it.first to System.currentTimeMillis()
                            } else {
                                it
                            }
                        }
                    }.andThen(Single.just(Unit.right()))
                }
            }
        }
    }
}
