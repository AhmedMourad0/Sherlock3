package dev.ahmedmourad.sherlock.domain.interactors.children

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.ChildrenRepository
import dev.ahmedmourad.sherlock.domain.model.children.Investigation
import io.reactivex.Flowable
import javax.inject.Inject

fun interface FindAllInvestigationsInteractor :
        () -> Flowable<Either<FindAllInvestigationsInteractor.Exception, List<Investigation>>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        object NoSignedInUserException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun ChildrenRepository.FindAllInvestigationsException.map() = when (this) {

    ChildrenRepository.FindAllInvestigationsException.NoInternetConnectionException ->
        FindAllInvestigationsInteractor.Exception.NoInternetConnectionException

    ChildrenRepository.FindAllInvestigationsException.NoSignedInUserException ->
        FindAllInvestigationsInteractor.Exception.NoSignedInUserException

    is ChildrenRepository.FindAllInvestigationsException.InternalException ->
        FindAllInvestigationsInteractor.Exception.InternalException(this.origin)

    is ChildrenRepository.FindAllInvestigationsException.UnknownException ->
        FindAllInvestigationsInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class FindAllInvestigationsInteractorImpl @Inject constructor(
        private val childrenRepository: Lazy<ChildrenRepository>
) : FindAllInvestigationsInteractor {
    override fun invoke():
            Flowable<Either<FindAllInvestigationsInteractor.Exception, List<Investigation>>> {
        return childrenRepository.get()
                .findAllInvestigations()
                .map { either ->
                    either.mapLeft(ChildrenRepository.FindAllInvestigationsException::map)
                }
    }
}
