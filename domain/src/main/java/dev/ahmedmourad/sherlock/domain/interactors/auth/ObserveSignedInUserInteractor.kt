package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Flowable

typealias ObserveSignedInUserInteractor =
        () -> @JvmSuppressWildcards Flowable<Either<Throwable, Either<IncompleteUser, SignedInUser>>>

internal class ObserveSignedInUserInteractorImpl(
        private val authManager: Lazy<AuthManager>
) : ObserveSignedInUserInteractor {
    override fun invoke(): Flowable<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get().observeSignedInUser()
    }
}
