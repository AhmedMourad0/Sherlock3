package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.ChildrenQuery
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import javax.inject.Inject

fun interface FindChildrenInteractor :
        (ChildrenQuery) ->
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
            query: ChildrenQuery
    ): Flowable<Either<FindChildrenInteractor.Exception, Map<SimpleRetrievedChild, Weight>>> {
        return childrenRepository.get()
                .findAll(query)
                .map { either ->
                    either.mapLeft(ChildrenRepository.FindAllException::map)
                }
    }
}
