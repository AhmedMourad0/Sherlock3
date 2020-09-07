package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import javax.inject.Inject

fun interface FindLastSearchResultsInteractor :
        () -> Flowable<Either<FindLastSearchResultsInteractor.Exception, Map<SimpleRetrievedChild, Weight>>> {
    sealed class Exception {
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun ChildrenRepository.FindLastSearchResultsException.map() = when (this) {

    is ChildrenRepository.FindLastSearchResultsException.InternalException ->
        FindLastSearchResultsInteractor.Exception.InternalException(this.origin)

    is ChildrenRepository.FindLastSearchResultsException.UnknownException ->
        FindLastSearchResultsInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class FindLastSearchResultsInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : FindLastSearchResultsInteractor {
    override fun invoke():
            Flowable<Either<FindLastSearchResultsInteractor.Exception, Map<SimpleRetrievedChild, Weight>>> {
        return childrenRepository.get()
                .findLastSearchResults()
                .map { either ->
                    either.mapLeft(ChildrenRepository.FindLastSearchResultsException::map)
                }
    }
}
