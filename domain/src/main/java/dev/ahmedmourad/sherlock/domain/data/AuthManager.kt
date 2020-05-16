package dev.ahmedmourad.sherlock.domain.data

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import io.reactivex.Flowable
import io.reactivex.Single

interface AuthManager {

    fun observeUserAuthState(): Flowable<Either<ObserveUserAuthStateException, Boolean>>

    fun observeSignedInUser():
            Flowable<Either<ObserveSignedInUserException, Either<IncompleteUser, SignedInUser>?>>

    fun signIn(
            credentials: UserCredentials
    ): Single<Either<SignInException, Either<IncompleteUser, SignedInUser>>>

    fun signUp(user: SignUpUser): Single<Either<SignUpException, SignedInUser>>

    fun completeSignUp(
            completedUser: CompletedUser
    ): Single<Either<CompleteSignUpException, SignedInUser>>

    fun signInWithGoogle(): Single<Either<SignInWithGoogleException, Either<IncompleteUser, SignedInUser>>>

    fun signInWithFacebook(): Single<Either<SignInWithFacebookException, Either<IncompleteUser, SignedInUser>>>

    fun signInWithTwitter(): Single<Either<SignInWithTwitterException, Either<IncompleteUser, SignedInUser>>>

    fun sendPasswordResetEmail(email: Email): Single<Either<SendPasswordResetEmailException, Unit>>

    fun signOut(): Single<Either<SignOutException, Unit>>

    sealed class ObserveUserAuthStateException {
        data class UnknownException(val origin: Throwable) : ObserveUserAuthStateException()
    }

    sealed class ObserveSignedInUserException {
        object NoInternetConnectionException : ObserveSignedInUserException()
        data class InternalException(val origin: Throwable) : ObserveSignedInUserException()
        data class UnknownException(val origin: Throwable) : ObserveSignedInUserException()
    }

    sealed class SignInException {
        object AccountDoesNotExistOrHasBeenDisabledException : SignInException()
        object WrongPasswordException : SignInException()
        object NoInternetConnectionException : SignInException()
        data class InternalException(val origin: Throwable) : SignInException()
        data class UnknownException(val origin: Throwable) : SignInException()
    }

    sealed class SignUpException {
        object WeakPasswordException : SignUpException()
        object MalformedEmailException : SignUpException()
        data class EmailAlreadyInUseException(val email: Email) : SignUpException()
        object NoInternetConnectionException : SignUpException()
        data class InternalException(val origin: Throwable) : SignUpException()
        data class UnknownException(val origin: Throwable) : SignUpException()
    }

    sealed class CompleteSignUpException {
        object NoInternetConnectionException : CompleteSignUpException()
        object NoSignedInUserException : CompleteSignUpException()
        data class InternalException(val origin: Throwable) : CompleteSignUpException()
        data class UnknownException(val origin: Throwable) : CompleteSignUpException()
    }

    sealed class SignInWithGoogleException {
        object AccountHasBeenDisabledException : SignInWithGoogleException()
        object MalformedOrExpiredCredentialException : SignInWithGoogleException()
        object EmailAlreadyInUseException : SignInWithGoogleException()
        object NoResponseException : SignInWithGoogleException()
        object NoInternetConnectionException : SignInWithGoogleException()
        data class InternalException(val origin: Throwable, val providerId: String) : SignInWithGoogleException()
        data class UnknownException(val origin: Throwable, val providerId: String) : SignInWithGoogleException()
    }

    sealed class SignInWithFacebookException {
        object AccountHasBeenDisabledException : SignInWithFacebookException()
        object MalformedOrExpiredCredentialException : SignInWithFacebookException()
        object EmailAlreadyInUseException : SignInWithFacebookException()
        object NoResponseException : SignInWithFacebookException()
        object NoInternetConnectionException : SignInWithFacebookException()
        data class InternalException(val origin: Throwable, val providerId: String) : SignInWithFacebookException()
        data class UnknownException(val origin: Throwable, val providerId: String) : SignInWithFacebookException()
    }

    sealed class SignInWithTwitterException {
        object AccountHasBeenDisabledException : SignInWithTwitterException()
        object MalformedOrExpiredCredentialException : SignInWithTwitterException()
        object EmailAlreadyInUseException : SignInWithTwitterException()
        object NoResponseException : SignInWithTwitterException()
        object NoInternetConnectionException : SignInWithTwitterException()
        data class InternalException(val origin: Throwable, val providerId: String) : SignInWithTwitterException()
        data class UnknownException(val origin: Throwable, val providerId: String) : SignInWithTwitterException()
    }

    sealed class SendPasswordResetEmailException {
        data class NonExistentEmailException(val email: Email) : SendPasswordResetEmailException()
        object NoInternetConnectionException : SendPasswordResetEmailException()
        data class UnknownException(val origin: Throwable) : SendPasswordResetEmailException()
    }

    sealed class SignOutException {
        object NoInternetConnectionException : SignOutException()
        data class InternalException(val origin: Throwable) : SignOutException()
        data class UnknownException(val origin: Throwable) : SignOutException()
    }
}
