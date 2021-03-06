package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single
import javax.inject.Inject

fun interface CompleteSignUpInteractor : (CompletedUser) -> Single<Either<CompleteSignUpInteractor.Exception, SignedInUser>> {
    sealed class Exception {
        object NoInternetConnectionException : Exception()
        object NoSignedInUserException : Exception()
        data class InternalException(val origin: Throwable) : Exception()
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun AuthManager.CompleteSignUpException.map() = when (this) {

    AuthManager.CompleteSignUpException.NoInternetConnectionException ->
        CompleteSignUpInteractor.Exception.NoInternetConnectionException

    AuthManager.CompleteSignUpException.NoSignedInUserException ->
        CompleteSignUpInteractor.Exception.NoSignedInUserException

    is AuthManager.CompleteSignUpException.InternalException ->
        CompleteSignUpInteractor.Exception.InternalException(this.origin)

    is AuthManager.CompleteSignUpException.UnknownException ->
        CompleteSignUpInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class CompleteSignUpInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : CompleteSignUpInteractor {
    override fun invoke(
            completedUser: CompletedUser
    ): Single<Either<CompleteSignUpInteractor.Exception, SignedInUser>> {
        return authManager.get()
                .completeSignUp(completedUser)
                .map { either ->
                    either.mapLeft(AuthManager.CompleteSignUpException::map)
                }
    }
}
