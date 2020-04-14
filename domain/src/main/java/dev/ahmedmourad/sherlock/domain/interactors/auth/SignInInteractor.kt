package dev.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import io.reactivex.Single

typealias SignInInteractor =
        (@JvmSuppressWildcards UserCredentials) ->
        @JvmSuppressWildcards Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>>

internal fun signIn(
        authManager: Lazy<AuthManager>,
        credentials: UserCredentials
): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
    return authManager.get().signIn(credentials)
}
