package dev.ahmedmourad.sherlock.children.local.repository

import arrow.core.*
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
import javax.inject.Inject

@Reusable
internal class RoomLocalRepository @Inject constructor(
        @InternalApi private val db: Lazy<ChildrenRoomDatabase>
) : LocalRepository {

    override fun updateIfExists(
            child: RetrievedChild
    ): Maybe<Either<Throwable, Tuple2<RetrievedChild, Weight?>>> {
        return db.get()
                .resultsDao()
                .updateIfExists(child.toRoomChildEntity(null))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(RoomChildEntity::toRetrievedChild)
    }

    override fun findAllWithWeight(): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> {
        return db.get()
                .resultsDao()
                .findAllWithWeight()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .distinctUntilChanged()
                .map { list ->
                    list.mapNotNull(RoomChildEntity::simplify)
                            .mapNotNull { tuple ->
                                tuple.b?.let { tuple.a toT it }
                            }.toMap().right()
                }
    }

    override fun replaceAll(
            results: Map<RetrievedChild, Weight>
    ): Single<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> {
        return db.get()
                .resultsDao()
                .replaceAll(results.map { (it.key toT it.value).toRoomChildEntity() })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toSingleDefault(results)
                .map { newValues ->
                    newValues.mapKeys { (child, _) ->
                        child.simplify()
                    }.right()
                }
    }

    override fun clear(): Completable {
        return Completable.fromAction { db.get().resultsDao().deleteAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }
}
