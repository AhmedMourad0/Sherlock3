package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import io.reactivex.Single
import javax.inject.Inject

fun interface AddChildInteractor :
        (ChildToPublish) -> Single<Either<AddChildInteractor.Exception, RetrievedChild>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        object NoSignedInUserException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun ChildrenRepository.PublishException.map() = when (this) {

    ChildrenRepository.PublishException.NoInternetConnectionException ->
        AddChildInteractor.Exception.NoInternetConnectionException

    ChildrenRepository.PublishException.NoSignedInUserException ->
        AddChildInteractor.Exception.NoSignedInUserException

    is ChildrenRepository.PublishException.InternalException ->
        AddChildInteractor.Exception.InternalException(this.origin)

    is ChildrenRepository.PublishException.UnknownException ->
        AddChildInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class AddChildInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : AddChildInteractor {
    override fun invoke(
            child: ChildToPublish
    ): Single<Either<AddChildInteractor.Exception, RetrievedChild>> {
        return childrenRepository.get()
                .publish(child)
                .map { either ->
                    either.mapLeft(ChildrenRepository.PublishException::map)
                }
    }
}
