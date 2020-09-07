package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Flowable
import javax.inject.Inject

fun interface ObserveSignedInUserInteractor :
        () -> Flowable<Either<ObserveSignedInUserInteractor.Exception, Either<IncompleteUser, SignedInUser>?>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.ObserveSignedInUserException.map() = when (this) {

    AuthManager.ObserveSignedInUserException.NoInternetConnectionException ->
        ObserveSignedInUserInteractor.Exception.NoInternetConnectionException

    is AuthManager.ObserveSignedInUserException.InternalException ->
        ObserveSignedInUserInteractor.Exception.InternalException(this.origin)

    is AuthManager.ObserveSignedInUserException.UnknownException ->
        ObserveSignedInUserInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class ObserveSignedInUserInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : ObserveSignedInUserInteractor {
    override fun invoke():
            Flowable<Either<ObserveSignedInUserInteractor.Exception, Either<IncompleteUser, SignedInUser>?>> {
        return authManager.get()
                .observeSignedInUser()
                .map { either ->
                    either.mapLeft(AuthManager.ObserveSignedInUserException::map)
                }
    }
}
