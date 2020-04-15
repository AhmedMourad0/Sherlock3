package dev.ahmedmourad.sherlock.domain.interactors.auth

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.data.AuthManager
import io.reactivex.Flowable
import javax.inject.Inject

typealias ObserveUserAuthStateInteractor = () -> @JvmSuppressWildcards Flowable<Boolean>

@Reusable
internal class ObserveUserAuthStateInteractorImpl @Inject constructor(
        private val authManager: Lazy<AuthManager>
) : ObserveUserAuthStateInteractor {
    override fun invoke(): Flowable<Boolean> {
        return authManager.get().observeUserAuthState()
    }
}
