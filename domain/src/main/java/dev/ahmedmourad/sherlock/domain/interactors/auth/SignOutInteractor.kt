package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Single

typealias SignOutInteractor =
        () -> @JvmSuppressWildcards Single<Either<Throwable, Unit>>

internal class SignOutInteractorImpl(
        private val authManager: Lazy<AuthManager>
) : SignOutInteractor {
    override fun invoke(): Single<Either<Throwable, Unit>> {
        return authManager.get().signOut()
    }
}
