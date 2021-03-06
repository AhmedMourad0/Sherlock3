package dev.ahmedmourad.sherlock.auth.manager.dependencies

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Flowable
import io.reactivex.Single

internal interface Authenticator {

    fun observeUserAuthState(): Flowable<Either<ObserveUserAuthStateException, Boolean>>

    fun observeSignedInUser(): Flowable<Either<ObserveSignedInUserException, IncompleteUser?>>

    fun signIn(credentials: UserCredentials): Single<Either<SignInException, IncompleteUser>>

    fun signUp(credentials: UserCredentials): Single<Either<SignUpException, IncompleteUser>>

    fun signInWithGoogle(): Single<Either<SignInWithGoogleException, IncompleteUser>>

    fun signInWithFacebook(): Single<Either<SignInWithFacebookException, IncompleteUser>>

    fun signInWithTwitter(): Single<Either<SignInWithTwitterException, IncompleteUser>>

    fun sendPasswordResetEmail(email: Email): Single<Either<SendPasswordResetEmailException, Unit>>

    fun signOut(): Single<Either<SignOutException, UserId?>>

    sealed class ObserveUserAuthStateException {
        object NoInternetConnectionException : ObserveUserAuthStateException()
        data class UnknownException(val origin: Throwable) : ObserveUserAuthStateException()
    }

    sealed class ObserveSignedInUserException {
        object NoInternetConnectionException : ObserveSignedInUserException()
        data class UnknownException(val origin: Throwable) : ObserveSignedInUserException()
    }

    sealed class SignInException {
        object AccountDoesNotExistOrHasBeenDisabledException : SignInException()
        object WrongPasswordException : SignInException()
        object NoInternetConnectionException : SignInException()
        data class UnknownException(val origin: Throwable) : SignInException()
    }

    sealed class SignUpException {
        object WeakPasswordException : SignUpException()
        object MalformedEmailException : SignUpException()
        data class EmailAlreadyInUseException(val email: Email) : SignUpException()
        object NoInternetConnectionException : SignUpException()
        data class UnknownException(val origin: Throwable) : SignUpException()
    }

    sealed class SignInWithGoogleException {
        object AccountHasBeenDisabledException : SignInWithGoogleException()
        object MalformedOrExpiredCredentialException : SignInWithGoogleException()
        object EmailAlreadyInUseException : SignInWithGoogleException()
        object NoResponseException : SignInWithGoogleException()
        object NoInternetConnectionException : SignInWithGoogleException()
        data class UnknownException(val origin: Throwable, val providerId: String) : SignInWithGoogleException()
    }

    sealed class SignInWithFacebookException {
        object AccountHasBeenDisabledException : SignInWithFacebookException()
        object MalformedOrExpiredCredentialException : SignInWithFacebookException()
        object EmailAlreadyInUseException : SignInWithFacebookException()
        object NoResponseException : SignInWithFacebookException()
        object NoInternetConnectionException : SignInWithFacebookException()
        data class UnknownException(val origin: Throwable, val providerId: String) : SignInWithFacebookException()
    }

    sealed class SignInWithTwitterException {
        object AccountHasBeenDisabledException : SignInWithTwitterException()
        object MalformedOrExpiredCredentialException : SignInWithTwitterException()
        object EmailAlreadyInUseException : SignInWithTwitterException()
        object NoResponseException : SignInWithTwitterException()
        object NoInternetConnectionException : SignInWithTwitterException()
        data class UnknownException(val origin: Throwable, val providerId: String) : SignInWithTwitterException()
    }

    sealed class SendPasswordResetEmailException {
        data class NonExistentEmailException(val email: Email) : SendPasswordResetEmailException()
        object NoInternetConnectionException : SendPasswordResetEmailException()
        data class UnknownException(val origin: Throwable) : SendPasswordResetEmailException()
    }

    sealed class SignOutException {
        object NoInternetConnectionException : SignOutException()
        data class UnknownException(val origin: Throwable) : SignOutException()
    }
}
