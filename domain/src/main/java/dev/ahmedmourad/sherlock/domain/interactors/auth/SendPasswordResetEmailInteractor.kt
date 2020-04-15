package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import io.reactivex.Single

typealias SendPasswordResetEmailInteractor = (@JvmSuppressWildcards Email) -> @JvmSuppressWildcards Single<Either<Throwable, Unit>>

internal class SendPasswordResetEmailInteractorImpl(
        private val authManager: Lazy<AuthManager>
) : SendPasswordResetEmailInteractor {
    override fun invoke(email: Email): Single<Either<Throwable, Unit>> {
        return authManager.get().sendPasswordResetEmail(email)
    }
}
