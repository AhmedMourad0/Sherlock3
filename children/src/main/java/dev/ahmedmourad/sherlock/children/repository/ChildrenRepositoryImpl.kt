package dev.ahmedmourad.sherlock.children.repository

import arrow.core.*
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.repository.dependencies.ImageRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.children.repository.dependencies.RemoteRepository
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.BackgroundState
import dev.ahmedmourad.sherlock.domain.constants.PublishingState
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.filter.Filter
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
        private val bus: Lazy<Bus>
) : ChildrenRepository {

    private val tester by lazy { SherlockTester(remoteRepository, localRepository) }

    override fun publish(
            child: PublishedChild
    ): Single<Either<ChildrenRepository.PublishException, RetrievedChild>> {

        fun ImageRepository.StoreChildPictureException.map() = when (this) {

            ImageRepository.StoreChildPictureException.NoInternetConnectionException ->
                ChildrenRepository.PublishException.NoInternetConnectionException

            ImageRepository.StoreChildPictureException.NoSignedInUserException ->
                ChildrenRepository.PublishException.NoSignedInUserException

            is ImageRepository.StoreChildPictureException.InternalException ->
                ChildrenRepository.PublishException.InternalException(this.origin)

            is ImageRepository.StoreChildPictureException.UnknownException ->
                ChildrenRepository.PublishException.UnknownException(this.origin)
        }

        fun RemoteRepository.PublishException.map() = when (this) {

            RemoteRepository.PublishException.NoInternetConnectionException ->
                ChildrenRepository.PublishException.NoInternetConnectionException

            RemoteRepository.PublishException.NoSignedInUserException ->
                ChildrenRepository.PublishException.NoSignedInUserException

            is RemoteRepository.PublishException.UnknownException ->
                ChildrenRepository.PublishException.UnknownException(this.origin)
        }

        fun ChildrenRepository.PublishException.toPublishStateException() = when (this) {

            ChildrenRepository.PublishException.NoInternetConnectionException ->
                PublishingState.Exception.NoInternetConnectionException

            ChildrenRepository.PublishException.NoSignedInUserException ->
                PublishingState.Exception.NoSignedInUserException

            is ChildrenRepository.PublishException.InternalException ->
                PublishingState.Exception.InternalException(this.origin)

            is ChildrenRepository.PublishException.UnknownException ->
                PublishingState.Exception.UnknownException(this.origin)
        }

        val childId = ChildId(UUID.randomUUID().toString())

        return imageRepository.get().storeChildPicture(childId, child.picture)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { urlEither ->
                    urlEither.fold(ifLeft = {
                        Single.just(it.map().left())
                    }, ifRight = {
                        remoteRepository.get()
                                .publish(childId, child, it)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .map { either ->
                                    either.mapLeft(RemoteRepository.PublishException::map)
                                }
                    })
                }.onErrorReturn {
                    ChildrenRepository.PublishException.UnknownException(it).left()
                }.doOnSuccess { childEither ->
                    childEither.fold(ifLeft = {
                        bus.get().childPublishingState.accept(
                                PublishingState.Failure(child, it.toPublishStateException())
                        )
                    }, ifRight = {
                        bus.get().childPublishingState.accept(PublishingState.Success(it))
                    })
                }.doOnSubscribe { bus.get().childPublishingState.accept(PublishingState.Ongoing(child)) }
                .doOnError {
                    bus.get().childPublishingState.accept(
                            PublishingState.Failure(
                                    child,
                                    ChildrenRepository.PublishException.UnknownException(it).toPublishStateException()
                            )
                    )
                }
    }

    override fun find(
            childId: ChildId
    ): Flowable<Either<ChildrenRepository.FindException, Tuple2<RetrievedChild, Weight?>?>> {

        fun RemoteRepository.FindException.map() = when (this) {

            RemoteRepository.FindException.NoInternetConnectionException ->
                ChildrenRepository.FindException.NoInternetConnectionException

            RemoteRepository.FindException.NoSignedInUserException ->
                ChildrenRepository.FindException.NoSignedInUserException

            is RemoteRepository.FindException.InternalException ->
                ChildrenRepository.FindException.InternalException(this.origin)

            is RemoteRepository.FindException.UnknownException ->
                ChildrenRepository.FindException.UnknownException(this.origin)
        }

        fun LocalRepository.UpdateIfExistsException.map() = when (this) {
            is LocalRepository.UpdateIfExistsException.InternalException ->
                ChildrenRepository.FindException.InternalException(this.origin)
        }

        return remoteRepository.get()
                .find(childId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { childEither ->
                    childEither.fold(ifLeft = {
                        Flowable.just(it.map().left())
                    }, ifRight = { child ->
                        if (child == null) {
                            Flowable.just(null.right())
                        } else {
                            localRepository.get()
                                    .updateIfExists(child)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io())
                                    .map { either ->
                                        either.mapLeft(LocalRepository.UpdateIfExistsException::map)
                                    }.toSingle((child toT null).right<Tuple2<RetrievedChild, Weight?>>())
                                    .toFlowable()
                        }
                    })
                }.onErrorReturn {
                    ChildrenRepository.FindException.UnknownException(it).left()
                }.doOnSubscribe { bus.get().childFindingState.accept(BackgroundState.ONGOING) }
                .doOnNext { bus.get().childFindingState.accept(BackgroundState.SUCCESS) }
                .doOnError { bus.get().childFindingState.accept(BackgroundState.FAILURE) }
    }

    override fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<ChildrenRepository.FindAllException, Map<SimpleRetrievedChild, Weight>>> {

        fun RemoteRepository.FindAllException.map() = when (this) {

            RemoteRepository.FindAllException.NoInternetConnectionException ->
                ChildrenRepository.FindAllException.NoInternetConnectionException

            RemoteRepository.FindAllException.NoSignedInUserException ->
                ChildrenRepository.FindAllException.NoSignedInUserException

            is RemoteRepository.FindAllException.UnknownException ->
                ChildrenRepository.FindAllException.UnknownException(this.origin)
        }

        return remoteRepository.get()
                .findAll(query, filter)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { resultsEither ->
                    resultsEither.fold(ifLeft = {
                        Flowable.just(it.map().left())
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
                }.onErrorReturn {
                    ChildrenRepository.FindAllException.UnknownException(it).left()
                }.doOnSubscribe { bus.get().childrenFindingState.accept(BackgroundState.ONGOING) }
                .doOnNext { bus.get().childrenFindingState.accept(BackgroundState.SUCCESS) }
                .doOnError { bus.get().childrenFindingState.accept(BackgroundState.FAILURE) }
    }

    override fun findLastSearchResults():
            Flowable<Either<ChildrenRepository.FindLastSearchResultsException, Map<SimpleRetrievedChild, Weight>>> {

        return localRepository.get()
                .findAllWithWeight()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map<Either<ChildrenRepository.FindLastSearchResultsException, Map<SimpleRetrievedChild, Weight>>> {
                    it.right()
                }.onErrorReturn {
                    ChildrenRepository.FindLastSearchResultsException.UnknownException(it).left()
                }
    }

    override fun test(): ChildrenRepository.Tester {
        return tester
    }

    class SherlockTester(
            private val remoteRepository: Lazy<RemoteRepository>,
            private val localRepository: Lazy<LocalRepository>
    ) : ChildrenRepository.Tester {
        override fun clear(): Single<Either<ChildrenRepository.Tester.ClearException, Unit>> {

            fun RemoteRepository.ClearException.map() = when (this) {

                RemoteRepository.ClearException.NoInternetConnectionException ->
                    ChildrenRepository.Tester.ClearException.NoInternetConnectionException

                RemoteRepository.ClearException.NoSignedInUserException ->
                    ChildrenRepository.Tester.ClearException.NoSignedInUserException

                is RemoteRepository.ClearException.UnknownException ->
                    ChildrenRepository.Tester.ClearException.UnknownException(this.origin)
            }

            return remoteRepository.get()
                    .clear()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap { either ->
                        either.fold(ifLeft = {
                            Single.just(it.map().left())
                        }, ifRight = {
                            localRepository.get().clear().toSingleDefault(Unit.right())
                        })
                    }.onErrorReturn {
                        ChildrenRepository.Tester.ClearException.UnknownException(it).left()
                    }
        }
    }
}
