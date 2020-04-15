package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single

typealias SignInWithGoogleInteractor =
        () -> @JvmSuppressWildcards Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>>

internal class SignInWithGoogleInteractorImpl(
        private val authManager: Lazy<AuthManager>
) : SignInWithGoogleInteractor {
    override fun invoke(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get().signInWithGoogle()
    }
}
