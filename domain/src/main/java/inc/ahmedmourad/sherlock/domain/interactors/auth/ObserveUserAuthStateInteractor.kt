package inc.ahmedmourad.sherlock.domain.interactors.auth

import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Flowable

typealias ObserveUserAuthStateInteractor = () -> @JvmSuppressWildcards Flowable<Boolean>

internal fun observeUserAuthState(authManager: Lazy<AuthManager>): Flowable<Boolean> {
    return authManager.get().observeUserAuthState()
}
