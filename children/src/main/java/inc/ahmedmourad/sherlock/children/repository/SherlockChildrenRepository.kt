package inc.ahmedmourad.sherlock.children.repository

import arrow.core.*
import dagger.Lazy
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenImageRepository
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenLocalRepository
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenRemoteRepository
import inc.ahmedmourad.sherlock.domain.constants.BackgroundState
import inc.ahmedmourad.sherlock.domain.constants.PublishingState
import inc.ahmedmourad.sherlock.domain.data.ChildrenRepository
import inc.ahmedmourad.sherlock.domain.filter.Filter
import inc.ahmedmourad.sherlock.domain.interactors.common.NotifyChildFindingStateChangeInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.NotifyChildPublishingStateChangeInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.NotifyChildrenFindingStateChangeInteractor
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.PublishedChild
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

//TODO: if requireUserSignedIn doesn't fail and user is not signed in with FirebaseAuth
// implement a fallback mechanism and sign the user in anonymously
internal class SherlockChildrenRepository(
        private val childrenLocalRepository: Lazy<ChildrenLocalRepository>,
        private val childrenRemoteRepository: Lazy<ChildrenRemoteRepository>,
        private val childrenImageRepository: Lazy<ChildrenImageRepository>,
        private val notifyChildPublishingStateChangeInteractor: NotifyChildPublishingStateChangeInteractor,
        private val notifyChildFindingStateChangeInteractor: NotifyChildFindingStateChangeInteractor,
        private val notifyChildrenFindingStateChangeInteractor: NotifyChildrenFindingStateChangeInteractor
) : ChildrenRepository {

    private val tester by lazy { SherlockTester(childrenRemoteRepository, childrenLocalRepository) }

    override fun publish(child: PublishedChild): Single<Either<Throwable, RetrievedChild>> {

        val childId = ChildId(UUID.randomUUID().toString())

        return childrenImageRepository.get().storeChildPicture(childId, child.picture)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { urlEither ->
                    urlEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        childrenRemoteRepository.get().publish(childId, child, it)
                    })
                }.doOnSuccess { childEither ->
                    childEither.fold(ifLeft = {
                        notifyChildPublishingStateChangeInteractor(PublishingState.Failure(child))
                    }, ifRight = {
                        notifyChildPublishingStateChangeInteractor(PublishingState.Success(it))
                    })
                }.doOnSubscribe { notifyChildPublishingStateChangeInteractor(PublishingState.Ongoing(child)) }
                .doOnError { notifyChildPublishingStateChangeInteractor(PublishingState.Failure(child)) }
    }

    override fun find(
            childId: ChildId
    ): Flowable<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>> {
        return childrenRemoteRepository.get()
                .find(childId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { childEither ->
                    childEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { child ->
                        if (child == null) {
                            Flowable.just(null.right())
                        } else {
                            childrenLocalRepository.get()
                                    .updateIfExists(child)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io())
                                    .toSingle((child toT null).right<Tuple2<RetrievedChild, Weight?>>())
                                    .toFlowable()
                        }
                    })
                }.doOnSubscribe { notifyChildFindingStateChangeInteractor(BackgroundState.ONGOING) }
                .doOnNext { notifyChildFindingStateChangeInteractor(BackgroundState.SUCCESS) }
                .doOnError { notifyChildFindingStateChangeInteractor(BackgroundState.FAILURE) }
    }

    override fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> {
        return childrenRemoteRepository.get()
                .findAll(query, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { resultsEither ->
                    resultsEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { results ->
                        childrenLocalRepository.get()
                                .replaceAll(results)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .map {
                                    results.mapKeys { (child, _) -> child.simplify() }
                                            .right()
                                }.toFlowable()
                    })
                }.doOnSubscribe { notifyChildrenFindingStateChangeInteractor(BackgroundState.ONGOING) }
                .doOnNext { notifyChildrenFindingStateChangeInteractor(BackgroundState.SUCCESS) }
                .doOnError { notifyChildrenFindingStateChangeInteractor(BackgroundState.FAILURE) }
    }

    override fun findLastSearchResults(): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> {
        return childrenLocalRepository.get()
                .findAllWithWeight()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    override fun test(): ChildrenRepository.Tester {
        return tester
    }

    class SherlockTester(
            private val childrenRemoteRepository: Lazy<ChildrenRemoteRepository>,
            private val childrenLocalRepository: Lazy<ChildrenLocalRepository>
    ) : ChildrenRepository.Tester {
        override fun clear(): Single<Either<Throwable, Unit>> {
            return childrenRemoteRepository.get()
                    .clear()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap { either ->
                        either.fold(ifLeft = {
                            Single.just(it.left())
                        }, ifRight = {
                            childrenLocalRepository.get()
                                    .clear()
                                    .andThen(Single.just(Unit.right()))
                        })
                    }
        }
    }
}
