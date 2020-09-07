package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import arrow.core.Tuple2
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.Flowable
import javax.inject.Inject

fun interface FindChildInteractor :
        (ChildId) -> Flowable<Either<FindChildInteractor.Exception, Tuple2<RetrievedChild, Weight?>?>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        object NoSignedInUserException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun ChildrenRepository.FindException.map() = when (this) {

    ChildrenRepository.FindException.NoInternetConnectionException ->
        FindChildInteractor.Exception.NoInternetConnectionException

    ChildrenRepository.FindException.NoSignedInUserException ->
        FindChildInteractor.Exception.NoSignedInUserException

    is ChildrenRepository.FindException.InternalException ->
        FindChildInteractor.Exception.InternalException(this.origin)

    is ChildrenRepository.FindException.UnknownException ->
        FindChildInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class FindChildInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : FindChildInteractor {
    override fun invoke(
            childId: ChildId
    ): Flowable<Either<FindChildInteractor.Exception, Tuple2<RetrievedChild, Weight?>?>> {
        return childrenRepository.get()
                .find(childId)
                .map { either ->
                    either.mapLeft(ChildrenRepository.FindException::map)
                }
    }
}
