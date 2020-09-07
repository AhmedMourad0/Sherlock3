package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import io.reactivex.Single
import javax.inject.Inject

fun interface SignInInteractor :
        (UserCredentials) -> Single<Either<SignInInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
    sealed class Exception {
        object AccountDoesNotExistOrHasBeenDisabledException : Exception()
        object WrongPasswordException : Exception()
        object NoInternetConnectionException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.SignInException.map() = when (this) {

    AuthManager.SignInException.AccountDoesNotExistOrHasBeenDisabledException ->
        SignInInteractor.Exception.AccountDoesNotExistOrHasBeenDisabledException

    AuthManager.SignInException.WrongPasswordException ->
        SignInInteractor.Exception.WrongPasswordException

    AuthManager.SignInException.NoInternetConnectionException ->
        SignInInteractor.Exception.NoInternetConnectionException

    is AuthManager.SignInException.InternalException ->
        SignInInteractor.Exception.InternalException(this.origin)

    is AuthManager.SignInException.UnknownException ->
        SignInInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class SignInInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignInInteractor {
    override fun invoke(
            credentials: UserCredentials
    ): Single<Either<SignInInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get()
                .signIn(credentials)
                .map { either ->
                    either.mapLeft(AuthManager.SignInException::map)
                }
    }
}
