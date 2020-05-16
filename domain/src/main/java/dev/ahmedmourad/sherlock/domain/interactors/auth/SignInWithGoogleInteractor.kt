package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single
import javax.inject.Inject

interface SignInWithGoogleInteractor :
        () -> Single<Either<SignInWithGoogleInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
    sealed class Exception {
        object AccountHasBeenDisabledException : Exception()
        object MalformedOrExpiredCredentialException : Exception()
        object EmailAlreadyInUseException : Exception()
        object NoResponseException : Exception()
        object NoInternetConnectionException : Exception()
        data class InternalException(val origin: Throwable, val providerId: String) : Exception()
        data class UnknownException(val origin: Throwable, val providerId: String) : Exception()
    }
}

private fun AuthManager.SignInWithGoogleException.map() = when (this) {

    AuthManager.SignInWithGoogleException.AccountHasBeenDisabledException ->
        SignInWithGoogleInteractor.Exception.AccountHasBeenDisabledException

    AuthManager.SignInWithGoogleException.MalformedOrExpiredCredentialException ->
        SignInWithGoogleInteractor.Exception.MalformedOrExpiredCredentialException

    AuthManager.SignInWithGoogleException.EmailAlreadyInUseException ->
        SignInWithGoogleInteractor.Exception.EmailAlreadyInUseException

    AuthManager.SignInWithGoogleException.NoResponseException ->
        SignInWithGoogleInteractor.Exception.NoResponseException

    AuthManager.SignInWithGoogleException.NoInternetConnectionException ->
        SignInWithGoogleInteractor.Exception.NoInternetConnectionException

    is AuthManager.SignInWithGoogleException.InternalException ->
        SignInWithGoogleInteractor.Exception.InternalException(this.origin, this.providerId)

    is AuthManager.SignInWithGoogleException.UnknownException ->
        SignInWithGoogleInteractor.Exception.UnknownException(this.origin, this.providerId)
}

@Reusable
internal class SignInWithGoogleInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignInWithGoogleInteractor {
    override fun invoke():
            Single<Either<SignInWithGoogleInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get()
                .signInWithGoogle()
                .map { either ->
                    either.mapLeft(AuthManager.SignInWithGoogleException::map)
                }
    }
}
