package dev.ahmedmourad.sherlock.children.local.repository

import arrow.core.*
import arrow.core.extensions.fx
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.children.di.InternalApi
import dev.ahmedmourad.sherlock.children.local.database.ChildrenRoomDatabase
import dev.ahmedmourad.sherlock.children.local.entities.RoomChildEntity
import dev.ahmedmourad.sherlock.children.local.mapper.toRoomChildEntity
import dev.ahmedmourad.sherlock.children.repository.dependencies.LocalRepository
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

@Reusable
internal class RoomLocalRepository @Inject constructor(
        @InternalApi private val db: Lazy<ChildrenRoomDatabase>
) : LocalRepository {

    override fun updateIfExists(
            child: RetrievedChild
    ): Maybe<Either<LocalRepository.UpdateIfExistsException, Tuple2<RetrievedChild, Weight?>>> {
        return db.get()
                .resultsDao()
                .updateIfExists(child.toRoomChildEntity(null))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map<Either<LocalRepository.UpdateIfExistsException, RoomChildEntity>> { it.right() }
                .onErrorReturn { LocalRepository.UpdateIfExistsException.UnknownException(it).left() }
                .map { either ->
                    Either.fx {
                        val (roomChild) = either
                        roomChild.toRetrievedChild()
                                .mapLeft {
                                    LocalRepository.UpdateIfExistsException.InternalException(it)
                                }.bind()
                    }
                }
    }

    override fun findAllWithWeight():
            Flowable<Either<LocalRepository.FindAllWithWeightException, Map<SimpleRetrievedChild, Weight>>> {
        return db.get()
                .resultsDao()
                .findAllWithWeight()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .distinctUntilChanged()
                .map<Either<LocalRepository.FindAllWithWeightException, List<RoomChildEntity>>> { it.right() }
                .onErrorReturn { LocalRepository.FindAllWithWeightException.UnknownException(it).left() }
                .map { either ->
                    either.map { list ->
                        list.map(RoomChildEntity::simplify)
                                .map { either ->
                                    either.mapLeft {
                                        LocalRepository.FindAllWithWeightException.InternalException(it)
                                    }
                                }.mapNotNull { either ->
                                    either.map { tuple ->
                                        tuple?.b?.let { tuple.a toT it }
                                    }.getOrHandle {
                                        Timber.error(it.origin, it::toString)
                                        null
                                    }
                                }.toMap()
                    }
                }
    }

    override fun replaceAll(
            results: Map<RetrievedChild, Weight>
    ): Single<Either<LocalRepository.ReplaceAllException, Map<SimpleRetrievedChild, Weight>>> {
        return db.get()
                .resultsDao()
                .replaceAll(results.map { (it.key toT it.value).toRoomChildEntity() })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toSingleDefault(results)
                .map<Either<LocalRepository.ReplaceAllException, Map<RetrievedChild, Weight>>> { it.right() }
                .onErrorReturn { LocalRepository.ReplaceAllException.UnknownException(it).left() }
                .map { either ->
                    either.map { newValues ->
                        newValues.mapKeys { (child, _) ->
                            child.simplify()
                        }
                    }
                }
    }

    override fun clear(): Single<Either<LocalRepository.ClearException, Unit>> {
        return Completable.fromAction { db.get().resultsDao().deleteAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toSingleDefault<Either<LocalRepository.ClearException, Unit>>(Unit.right())
                .onErrorReturn { LocalRepository.ClearException.UnknownException(it).left() }
    }
}
