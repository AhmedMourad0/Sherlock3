package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Single
import javax.inject.Inject

typealias SignOutInteractor =
        () -> @JvmSuppressWildcards Single<Either<Throwable, Unit>>

@Reusable
internal class SignOutInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignOutInteractor {
    override fun invoke(): Single<Either<Throwable, Unit>> {
        return authManager.get().signOut()
    }
}
