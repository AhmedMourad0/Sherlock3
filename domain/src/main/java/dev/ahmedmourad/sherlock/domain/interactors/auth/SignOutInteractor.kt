package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Single
import javax.inject.Inject

fun interface SignOutInteractor : () -> Single<Either<SignOutInteractor.Exception, Unit>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.SignOutException.map() = when (this) {

    AuthManager.SignOutException.NoInternetConnectionException ->
        SignOutInteractor.Exception.NoInternetConnectionException

    is AuthManager.SignOutException.InternalException ->
        SignOutInteractor.Exception.InternalException(this.origin)

    is AuthManager.SignOutException.UnknownException ->
        SignOutInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class SignOutInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignOutInteractor {
    override fun invoke(): Single<Either<SignOutInteractor.Exception, Unit>> {
        return authManager.get()
                .signOut()
                .map { either ->
                    either.mapLeft(AuthManager.SignOutException::map)
                }
    }
}
