package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import io.reactivex.Single
import javax.inject.Inject

typealias SendPasswordResetEmailInteractor = (@JvmSuppressWildcards Email) -> @JvmSuppressWildcards Single<Either<Throwable, Unit>>

@Reusable
internal class SendPasswordResetEmailInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SendPasswordResetEmailInteractor {
    override fun invoke(email: Email): Single<Either<Throwable, Unit>> {
        return authManager.get().sendPasswordResetEmail(email)
    }
}
