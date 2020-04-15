package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable

typealias FindLastSearchResultsInteractor =
        () -> @JvmSuppressWildcards Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>>

internal class FindLastSearchResultsInteractorImpl(
        private val childrenRepository: Lazy<ChildrenRepository>
) : FindLastSearchResultsInteractor {
    override fun invoke(): Flowable<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> {
        return childrenRepository.get().findLastSearchResults()
    }
}
