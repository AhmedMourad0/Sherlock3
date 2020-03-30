package inc.ahmedmourad.sherlock.domain.interactors.auth

import arrow.core.Either
import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Single

typealias SignOutInteractor = () -> @JvmSuppressWildcards Single<Either<Throwable, Unit>>

internal fun signOut(authManager: Lazy<AuthManager>): Single<Either<Throwable, Unit>> {
    return authManager.get().signOut()
}
