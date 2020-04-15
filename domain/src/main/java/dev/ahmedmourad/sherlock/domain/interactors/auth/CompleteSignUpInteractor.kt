package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single

typealias CompleteSignUpInteractor =
        (@JvmSuppressWildcards CompletedUser) -> @JvmSuppressWildcards Single<Either<Throwable, SignedInUser>>

internal class CompleteSignUpInteractorImpl(
        private val authManager: Lazy<AuthManager>
) : CompleteSignUpInteractor {
    override fun invoke(completedUser: CompletedUser): Single<Either<Throwable, SignedInUser>> {
        return authManager.get().completeSignUp(completedUser)
    }
}
