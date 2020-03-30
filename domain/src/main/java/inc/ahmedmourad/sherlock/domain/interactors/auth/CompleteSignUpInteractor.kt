package inc.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single

typealias CompleteSignUpInteractor =
        (@JvmSuppressWildcards CompletedUser) -> @JvmSuppressWildcards Single<Either<Throwable, SignedInUser>>

internal fun completeSignUp(
        authManager: Lazy<AuthManager>,
        completedUser: CompletedUser
): Single<Either<Throwable, SignedInUser>> {
    return authManager.get().completeSignUp(completedUser)
}
