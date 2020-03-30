package inc.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Flowable

typealias FindSignedInUserInteractor =
        () -> @JvmSuppressWildcards Flowable<Either<Throwable, Either<IncompleteUser, SignedInUser>>>

internal fun findSignedInUser(
        authManager: Lazy<AuthManager>
): Flowable<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
    return authManager.get().findSignedInUser()
}
