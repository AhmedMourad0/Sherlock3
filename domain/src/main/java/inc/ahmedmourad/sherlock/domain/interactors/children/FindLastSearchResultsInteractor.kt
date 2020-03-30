package inc.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import arrow.core.Tuple2
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.ChildrenRepository
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable

typealias FindLastSearchResultsInteractor =
        () -> @JvmSuppressWildcards Flowable<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight>>>>

internal fun findLastSearchResults(
        childrenRepository: Lazy<ChildrenRepository>
): Flowable<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight>>>> {
    return childrenRepository.get().findLastSearchResults()
}
