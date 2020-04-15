package dev.ahmedmourad.sherlock.domain.interactors.auth

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Flowable

typealias ObserveUserAuthStateInteractor = () -> @JvmSuppressWildcards Flowable<Boolean>

internal class ObserveUserAuthStateInteractorImpl(
        private val authManager: Lazy<AuthManager>
) : ObserveUserAuthStateInteractor {
    override fun invoke(): Flowable<Boolean> {
        return authManager.get().observeUserAuthState()
    }
}
