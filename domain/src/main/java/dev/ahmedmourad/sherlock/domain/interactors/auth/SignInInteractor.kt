package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import io.reactivex.Single
import javax.inject.Inject

interface SignInInteractor :
        (UserCredentials) -> Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>>

@Reusable
internal class SignInInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : SignInInteractor {
    override fun invoke(
            credentials: UserCredentials
    ): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return authManager.get().signIn(credentials)
    }
}
