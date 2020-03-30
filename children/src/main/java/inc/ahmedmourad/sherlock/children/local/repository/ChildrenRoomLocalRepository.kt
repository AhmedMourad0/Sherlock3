package inc.ahmedmourad.sherlock.children.local.repository

import arrow.core.*
import dagger.Lazy
import inc.ahmedmourad.sherlock.children.local.database.SherlockDatabase
import inc.ahmedmourad.sherlock.children.local.entities.RoomChildEntity
import inc.ahmedmourad.sherlock.children.local.mapper.toRoomChildEntity
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenLocalRepository
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import timber.log.error

internal class ChildrenRoomLocalRepository(private val db: Lazy<SherlockDatabase>) : ChildrenLocalRepository {

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

    override fun findAllWithWeight(): Flowable<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight>>>> {
        return db.get()
                .resultsDao()
                .findAllWithWeight()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .distinctUntilChanged()
                .map { list ->
                    list.mapNotNull { child ->
                        child.simplify().getOrHandle {
                            Timber.error(it, it::toString)
                            null
                        }
                    }.mapNotNull { tuple ->
                        tuple.b?.let { tuple.a toT it }
                    }.right()
                }
    }

    override fun replaceAll(
            results: List<Tuple2<RetrievedChild, Weight>>
    ): Single<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight?>>>> {
        return db.get()
                .resultsDao()
                .replaceAll(results.map(Tuple2<RetrievedChild, Weight>::toRoomChildEntity))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toSingleDefault(results)
                .map { newValues ->
                    newValues.mapNotNull { (child, weight) ->
                        child.toRoomChildEntity(weight).simplify().getOrHandle {
                            Timber.error(it, it::toString)
                            null
                        }
                    }.right()
                }
    }

    override fun clear(): Completable {
        return Completable.fromAction { db.get().resultsDao().deleteAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }
}
