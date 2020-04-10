package inc.ahmedmourad.sherlock.domain.data

import arrow.core.Either
import arrow.core.Tuple2
import inc.ahmedmourad.sherlock.domain.filter.Filter
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.PublishedChild
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable
import io.reactivex.Single

interface ChildrenRepository {

    fun publish(child: PublishedChild): Single<Either<Throwable, RetrievedChild>>

    fun find(
            childId: ChildId
    ): Flowable<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>>

    fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>>

    fun findLastSearchResults(): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>>

    fun test(): Tester

    interface Tester {
        fun clear(): Single<Either<Throwable, Unit>>
    }
}
