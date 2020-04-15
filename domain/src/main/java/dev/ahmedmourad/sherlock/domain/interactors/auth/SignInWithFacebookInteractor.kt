package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single
import javax.inject.Inject

interface SignInWithFacebookInteractor :
        () -> Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>>

@Reusable
internal class SignInWithFacebookInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignInWithFacebookInteractor {
    override fun invoke(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get().signInWithFacebook()
    }
}
