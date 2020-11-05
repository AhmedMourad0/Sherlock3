package dev.ahmedmourad.sherlock.children.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.orNull
import arrow.core.right
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.model.children.*
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

internal class FakeRemoteRepository : RemoteRepository {

    private val fakeChildrenDb = mutableListOf<RetrievedChild>()
    private val fakeQueriesDb = mutableListOf<ChildrenQuery>()
    private val fakeInvestigationsDb = mutableListOf<Investigation>()

    var hasInternet = true
    var isUserSignedIn = true
    var triggerInternalException = false
    var triggerUnknownException = false

    override fun publish(
            childId: ChildId,
            child: ChildToPublish,
            pictureUrl: Url?
    ): Single<Either<RemoteRepository.PublishException, RetrievedChild>> {
        return Single.defer {
            when {

                !hasInternet -> {
                    Single.just(RemoteRepository.PublishException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Single.just(RemoteRepository.PublishException.NoSignedInUserException.left())
                }

                triggerUnknownException -> {
                    Single.just(RemoteRepository.PublishException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    val retrieved = child.toRetrievedChild(childId, System.currentTimeMillis(), pictureUrl)
                    fakeChildrenDb.add(retrieved)
                    Single.just(retrieved.right())
                }
            }
        }
    }

    override fun find(
            childId: ChildId
    ): Flowable<Either<RemoteRepository.FindException, RetrievedChild?>> {
        return Flowable.defer {
            when {

                !hasInternet -> {
                    Flowable.just(RemoteRepository.FindException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Flowable.just(RemoteRepository.FindException.NoSignedInUserException.left())
                }

                triggerInternalException -> {
                    Flowable.just(RemoteRepository.FindException.InternalException(RuntimeException()).left())
                }

                triggerUnknownException -> {
                    Flowable.just(RemoteRepository.FindException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    Flowable.just(fakeChildrenDb.firstOrNull {
                        it.id == childId
                    }.right())
                }
            }
        }
    }

    override fun addInvestigation(
            investigation: Investigation
    ): Single<Either<RemoteRepository.AddInvestigationException, Investigation>> {
        return Single.defer {
            when {

                !hasInternet -> {
                    Single.just(RemoteRepository.AddInvestigationException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Single.just(RemoteRepository.AddInvestigationException.NoSignedInUserException.left())
                }

                triggerUnknownException -> {
                    Single.just(RemoteRepository.AddInvestigationException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    fakeInvestigationsDb.add(investigation)
                    Single.just(investigation.right())
                }
            }
        }
    }

    override fun findAllInvestigations():
            Flowable<Either<RemoteRepository.FindAllInvestigationsException, List<Investigation>>> {
        return Flowable.defer {
            when {

                !hasInternet -> {
                    Flowable.just(RemoteRepository.FindAllInvestigationsException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Flowable.just(RemoteRepository.FindAllInvestigationsException.NoSignedInUserException.left())
                }

                triggerInternalException -> {
                    Flowable.just(RemoteRepository.FindAllInvestigationsException.InternalException(RuntimeException()).left())
                }

                triggerUnknownException -> {
                    Flowable.just(RemoteRepository.FindAllInvestigationsException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    Flowable.just(fakeInvestigationsDb.right())
                }
            }
        }
    }

    override fun findAll(
            query: ChildrenQuery
    ): Flowable<Either<RemoteRepository.FindAllException, Map<SimpleRetrievedChild, Weight>>> {
        return Flowable.defer {
            when {

                !hasInternet -> {
                    Flowable.just(RemoteRepository.FindAllException.NoInternetConnectionException.left())
                }

                !isUserSignedIn -> {
                    Flowable.just(RemoteRepository.FindAllException.NoSignedInUserException.left())
                }

                triggerInternalException -> {
                    Flowable.just(RemoteRepository.FindAllException.InternalException(RuntimeException()).left())
                }

                triggerUnknownException -> {
                    Flowable.just(RemoteRepository.FindAllException.UnknownException(RuntimeException()).left())
                }

                else -> {
                    fakeQueriesDb.add(query)
                    Flowable.just(fakeChildrenDb.dropLast(query.page * 20)
                            .takeLast(20)
                            .map(RetrievedChild::simplify)
                            .associateWith {
                                Weight.of((700..1000).random() / 1000.0).orNull()!!
                            }.right()
                    )
                }
            }
        }
    }

    override fun invalidateAllQueries(): Completable {
        return Completable.fromAction {
            fakeQueriesDb.clear()
        }
    }

    fun allChildren(): List<RetrievedChild> {
        return fakeChildrenDb
    }

    fun allInvestigations(): List<Investigation> {
        return fakeInvestigationsDb
    }

    fun allQueries(): List<ChildrenQuery> {
        return fakeQueriesDb
    }
}
