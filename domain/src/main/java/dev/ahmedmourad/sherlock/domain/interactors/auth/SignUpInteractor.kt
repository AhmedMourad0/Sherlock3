package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import io.reactivex.Single
import javax.inject.Inject

fun interface SignUpInteractor :
        (SignUpUser) -> Single<Either<SignUpInteractor.Exception, SignedInUser>> {
    sealed class Exception {
        object WeakPasswordException : Exception()
        object MalformedEmailException : Exception()
        data class EmailAlreadyInUseException(val email: Email) : Exception()
        object NoInternetConnectionException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.SignUpException.map() = when (this) {

    AuthManager.SignUpException.WeakPasswordException ->
        SignUpInteractor.Exception.WeakPasswordException

    AuthManager.SignUpException.MalformedEmailException ->
        SignUpInteractor.Exception.MalformedEmailException

    is AuthManager.SignUpException.EmailAlreadyInUseException ->
        SignUpInteractor.Exception.EmailAlreadyInUseException(this.email)

    AuthManager.SignUpException.NoInternetConnectionException ->
        SignUpInteractor.Exception.NoInternetConnectionException

    is AuthManager.SignUpException.InternalException ->
        SignUpInteractor.Exception.InternalException(this.origin)

    is AuthManager.SignUpException.UnknownException ->
        SignUpInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class SignUpInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignUpInteractor {
    override fun invoke(
            user: SignUpUser
    ): Single<Either<SignUpInteractor.Exception, SignedInUser>> {
        return authManager.get()
                .signUp(user)
                .map { either ->
                    either.mapLeft(AuthManager.SignUpException::map)
                }
    }
}
