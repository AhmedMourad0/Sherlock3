package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.filter.Filter
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import javax.inject.Inject

interface FindChildrenInteractor :
        (ChildQuery, Filter<RetrievedChild>) ->
        Flowable<Either<FindChildrenInteractor.Exception, Map<SimpleRetrievedChild, Weight>>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        object NoSignedInUserException : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun ChildrenRepository.FindAllException.map() = when (this) {

    ChildrenRepository.FindAllException.NoInternetConnectionException ->
        FindChildrenInteractor.Exception.NoInternetConnectionException

    ChildrenRepository.FindAllException.NoSignedInUserException ->
        FindChildrenInteractor.Exception.NoSignedInUserException

    is ChildrenRepository.FindAllException.UnknownException ->
        FindChildrenInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class FindChildrenInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : FindChildrenInteractor {
    override fun invoke(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<FindChildrenInteractor.Exception, Map<SimpleRetrievedChild, Weight>>> {
        return childrenRepository.get()
                .findAll(query, filter)
                .map { either ->
                    either.mapLeft(ChildrenRepository.FindAllException::map)
                }
    }
}
