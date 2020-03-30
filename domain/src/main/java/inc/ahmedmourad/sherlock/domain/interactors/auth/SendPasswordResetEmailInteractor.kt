package inc.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import io.reactivex.Single

typealias SendPasswordResetEmailInteractor = (@JvmSuppressWildcards Email) -> @JvmSuppressWildcards Single<Either<Throwable, Unit>>

internal fun sendPasswordResetEmail(
        authManager: Lazy<AuthManager>,
        email: Email
): Single<Either<Throwable, Unit>> {
    return authManager.get().sendPasswordResetEmail(email)
}
