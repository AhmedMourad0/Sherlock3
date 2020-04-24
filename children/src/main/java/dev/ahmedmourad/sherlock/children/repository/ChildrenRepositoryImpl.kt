package dev.ahmedmourad.sherlock.children.repository

import arrow.core.*
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.dagger.InternalApi
import dev.ahmedmourad.sherlock.children.repository.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.interactors.common.NotifyChildFindingStateChangeInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.NotifyChildPublishingStateChangeInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.NotifyChildrenFindingStateChangeInteractor
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.PublishedChild
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

//TODO: if requireUserSignedIn doesn't fail and user is not signed in with FirebaseAuth
// implement a fallback mechanism and sign the user in anonymously
@Reusable
internal class ChildrenRepositoryImpl @Inject constructor(
        @InternalApi private val localRepository: Lazy<LocalRepository>,
        @InternalApi private val remoteRepository: Lazy<RemoteRepository>,
        @InternalApi private val imageRepository: Lazy<ImageRepository>,
        private val notifyChildPublishingStateChangeInteractor: NotifyChildPublishingStateChangeInteractor,
        private val notifyChildFindingStateChangeInteractor: NotifyChildFindingStateChangeInteractor,
        private val notifyChildrenFindingStateChangeInteractor: NotifyChildrenFindingStateChangeInteractor
) : ChildrenRepository {

    private val tester by lazy { SherlockTester(remoteRepository, localRepository) }

    override fun publish(child: PublishedChild): Single<Either<Throwable, RetrievedChild>> {

        val childId = ChildId(UUID.randomUUID().toString())

        return imageRepository.get().storeChildPicture(childId, child.picture)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { urlEither ->
                    urlEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = {
                        remoteRepository.get().publish(childId, child, it)
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
        return remoteRepository.get()
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
                            localRepository.get()
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
        return remoteRepository.get()
                .findAll(query, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { resultsEither ->
                    resultsEither.fold(ifLeft = {
                        Flowable.just(it.left())
                    }, ifRight = { results ->
                        localRepository.get()
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
        return localRepository.get()
                .findAllWithWeight()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    override fun test(): ChildrenRepository.Tester {
        return tester
    }

    class SherlockTester(
            private val remoteRepository: Lazy<RemoteRepository>,
            private val localRepository: Lazy<LocalRepository>
    ) : ChildrenRepository.Tester {
        override fun clear(): Single<Either<Throwable, Unit>> {
            return remoteRepository.get()
                    .clear()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap { either ->
                        either.fold(ifLeft = {
                            Single.just(it.left())
                        }, ifRight = {
                            localRepository.get()
                                    .clear()
                                    .andThen(Single.just(Unit.right()))
                        })
                    }
        }
    }
}
