package inc.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import arrow.core.Tuple2
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.ChildrenRepository
import inc.ahmedmourad.sherlock.domain.filter.Filter
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable

typealias FindChildrenInteractor =
        (@JvmSuppressWildcards ChildQuery, @JvmSuppressWildcards Filter<RetrievedChild>) ->
        @JvmSuppressWildcards Flowable<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight>>>>

internal fun findChildren(
        childrenRepository: Lazy<ChildrenRepository>,
        query: ChildQuery,
        filter: Filter<RetrievedChild>
): Flowable<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight>>>> {
    return childrenRepository.get().findAll(query, filter)
}
