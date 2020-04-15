package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single
import javax.inject.Inject

interface SignUpInteractor : (SignUpUser) -> Single<Either<Throwable, SignedInUser>>

@Reusable
internal class SignUpInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignUpInteractor {
    override fun invoke(user: SignUpUser): Single<Either<Throwable, SignedInUser>> {
        return authManager.get().signUp(user)
    }
}
