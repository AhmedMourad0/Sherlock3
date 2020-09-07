package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.Investigation
import io.reactivex.Single
import javax.inject.Inject

fun interface AddInvestigationInteractor :
        (Investigation) -> Single<Either<AddInvestigationInteractor.Exception, Investigation>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        object NoSignedInUserException : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun ChildrenRepository.AddInvestigationException.map() = when (this) {

    ChildrenRepository.AddInvestigationException.NoInternetConnectionException ->
        AddInvestigationInteractor.Exception.NoInternetConnectionException

    ChildrenRepository.AddInvestigationException.NoSignedInUserException ->
        AddInvestigationInteractor.Exception.NoSignedInUserException

    is ChildrenRepository.AddInvestigationException.UnknownException ->
        AddInvestigationInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class AddInvestigationInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : AddInvestigationInteractor {
    override fun invoke(
            investigation: Investigation
    ): Single<Either<AddInvestigationInteractor.Exception, Investigation>> {
        return childrenRepository.get()
                .addInvestigation(investigation)
                .map { either ->
                    either.mapLeft(ChildrenRepository.AddInvestigationException::map)
                }
    }
}
