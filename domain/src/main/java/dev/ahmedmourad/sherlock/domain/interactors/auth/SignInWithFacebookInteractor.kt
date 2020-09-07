package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single
import javax.inject.Inject

fun interface SignInWithFacebookInteractor :
        () -> Single<Either<SignInWithFacebookInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
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

private fun AuthManager.SignInWithFacebookException.map() = when (this) {

    AuthManager.SignInWithFacebookException.AccountHasBeenDisabledException ->
        SignInWithFacebookInteractor.Exception.AccountHasBeenDisabledException

    AuthManager.SignInWithFacebookException.MalformedOrExpiredCredentialException ->
        SignInWithFacebookInteractor.Exception.MalformedOrExpiredCredentialException

    AuthManager.SignInWithFacebookException.EmailAlreadyInUseException ->
        SignInWithFacebookInteractor.Exception.EmailAlreadyInUseException

    AuthManager.SignInWithFacebookException.NoResponseException ->
        SignInWithFacebookInteractor.Exception.NoResponseException

    AuthManager.SignInWithFacebookException.NoInternetConnectionException ->
        SignInWithFacebookInteractor.Exception.NoInternetConnectionException

    is AuthManager.SignInWithFacebookException.InternalException ->
        SignInWithFacebookInteractor.Exception.InternalException(this.origin, this.providerId)

    is AuthManager.SignInWithFacebookException.UnknownException ->
        SignInWithFacebookInteractor.Exception.UnknownException(this.origin, this.providerId)
}

@Reusable
internal class SignInWithFacebookInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignInWithFacebookInteractor {
    override fun invoke():
            Single<Either<SignInWithFacebookInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get()
                .signInWithFacebook()
                .map { either ->
                    either.mapLeft(AuthManager.SignInWithFacebookException::map)
                }
    }
}
