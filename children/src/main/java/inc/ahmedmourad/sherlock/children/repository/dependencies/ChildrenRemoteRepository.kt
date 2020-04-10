package inc.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import inc.ahmedmourad.sherlock.domain.filter.Filter
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.PublishedChild
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable
import io.reactivex.Single

internal interface ChildrenRemoteRepository {

    fun publish(
            childId: ChildId,
            child: PublishedChild,
            pictureUrl: Url?
    ): Single<Either<Throwable, RetrievedChild>>

    fun find(
            childId: ChildId
    ): Flowable<Either<Throwable, RetrievedChild?>>

    fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<Throwable, Map<RetrievedChild, Weight>>>

    fun clear(): Single<Either<Throwable, Unit>>
}
