package inc.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
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
        @JvmSuppressWildcards Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>>

internal fun findChildren(
        childrenRepository: Lazy<ChildrenRepository>,
        query: ChildQuery,
        filter: Filter<RetrievedChild>
): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> {
    return childrenRepository.get().findAll(query, filter)
}
