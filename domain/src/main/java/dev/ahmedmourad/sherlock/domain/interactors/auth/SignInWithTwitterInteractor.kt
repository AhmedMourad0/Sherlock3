package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single
import javax.inject.Inject

interface SignInWithTwitterInteractor :
        () -> Single<Either<SignInWithTwitterInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
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

private fun AuthManager.SignInWithTwitterException.map() = when (this) {

    AuthManager.SignInWithTwitterException.AccountHasBeenDisabledException ->
        SignInWithTwitterInteractor.Exception.AccountHasBeenDisabledException

    AuthManager.SignInWithTwitterException.MalformedOrExpiredCredentialException ->
        SignInWithTwitterInteractor.Exception.MalformedOrExpiredCredentialException

    AuthManager.SignInWithTwitterException.EmailAlreadyInUseException ->
        SignInWithTwitterInteractor.Exception.EmailAlreadyInUseException

    AuthManager.SignInWithTwitterException.NoResponseException ->
        SignInWithTwitterInteractor.Exception.NoResponseException

    AuthManager.SignInWithTwitterException.NoInternetConnectionException ->
        SignInWithTwitterInteractor.Exception.NoInternetConnectionException

    is AuthManager.SignInWithTwitterException.InternalException ->
        SignInWithTwitterInteractor.Exception.InternalException(this.origin, this.providerId)

    is AuthManager.SignInWithTwitterException.UnknownException ->
        SignInWithTwitterInteractor.Exception.UnknownException(this.origin, this.providerId)
}

@Reusable
internal class SignInWithTwitterInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignInWithTwitterInteractor {
    override fun invoke():
            Single<Either<SignInWithTwitterInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get()
                .signInWithTwitter()
                .map { either ->
                    either.mapLeft(AuthManager.SignInWithTwitterException::map)
                }
    }
}
