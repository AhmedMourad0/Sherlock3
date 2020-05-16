package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import io.reactivex.Single
import javax.inject.Inject

interface SendPasswordResetEmailInteractor :
        (Email) -> Single<Either<SendPasswordResetEmailInteractor.Exception, Unit>> {
    sealed class Exception {
        data class NonExistentEmailException(val email: Email) : Exception()
        object NoInternetConnectionException : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.SendPasswordResetEmailException.map() = when (this) {

    is AuthManager.SendPasswordResetEmailException.NonExistentEmailException ->
        SendPasswordResetEmailInteractor.Exception.NonExistentEmailException(this.email)

    AuthManager.SendPasswordResetEmailException.NoInternetConnectionException ->
        SendPasswordResetEmailInteractor.Exception.NoInternetConnectionException

    is AuthManager.SendPasswordResetEmailException.UnknownException ->
        SendPasswordResetEmailInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class SendPasswordResetEmailInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SendPasswordResetEmailInteractor {
    override fun invoke(
            email: Email
    ): Single<Either<SendPasswordResetEmailInteractor.Exception, Unit>> {
        return authManager.get()
                .sendPasswordResetEmail(email)
                .map { either ->
                    either.mapLeft(AuthManager.SendPasswordResetEmailException::map)
                }
    }
}
