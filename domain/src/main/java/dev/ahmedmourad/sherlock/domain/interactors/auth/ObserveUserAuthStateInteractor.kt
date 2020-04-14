package dev.ahmedmourad.sherlock.domain.interactors.auth

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Flowable

typealias ObserveUserAuthStateInteractor = () -> @JvmSuppressWildcards Flowable<Boolean>

internal fun observeUserAuthState(authManager: Lazy<AuthManager>): Flowable<Boolean> {
    return authManager.get().observeUserAuthState()
}
