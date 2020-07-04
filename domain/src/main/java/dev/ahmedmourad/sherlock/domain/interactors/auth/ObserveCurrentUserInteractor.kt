package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Flowable
import javax.inject.Inject

interface ObserveCurrentUserInteractor :
        () -> Flowable<Either<ObserveCurrentUserInteractor.Exception, Either<IncompleteUser, SignedInUser>?>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.ObserveCurrentUserException.map() = when (this) {

    AuthManager.ObserveCurrentUserException.NoInternetConnectionException ->
        ObserveCurrentUserInteractor.Exception.NoInternetConnectionException

    is AuthManager.ObserveCurrentUserException.InternalException ->
        ObserveCurrentUserInteractor.Exception.InternalException(this.origin)

    is AuthManager.ObserveCurrentUserException.UnknownException ->
        ObserveCurrentUserInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class ObserveCurrentUserInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : ObserveCurrentUserInteractor {
    override fun invoke():
            Flowable<Either<ObserveCurrentUserInteractor.Exception, Either<IncompleteUser, SignedInUser>?>> {
        return authManager.get()
                .observeSignedInUser()
                .map { either ->
                    either.mapLeft(AuthManager.ObserveCurrentUserException::map)
                }
    }
}
