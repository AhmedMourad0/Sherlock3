package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single

typealias SignUpInteractor =
        (@JvmSuppressWildcards SignUpUser) -> @JvmSuppressWildcards Single<Either<Throwable, SignedInUser>>

internal class SignUpInteractorImpl(
        private val authManager: Lazy<AuthManager>
) : SignUpInteractor {
    override fun invoke(user: SignUpUser): Single<Either<Throwable, SignedInUser>> {
        return authManager.get().signUp(user)
    }
}
