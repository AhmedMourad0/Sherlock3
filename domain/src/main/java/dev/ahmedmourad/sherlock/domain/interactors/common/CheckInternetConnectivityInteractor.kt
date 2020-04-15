package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Single

typealias CheckInternetConnectivityInteractor =
        () -> @JvmSuppressWildcards Single<Boolean>

internal class CheckInternetConnectivityInteractorImpl(
        private val connectivityManager: Lazy<ConnectivityManager>
) : CheckInternetConnectivityInteractor {
    override fun invoke(): Single<Boolean> {
        return connectivityManager.get().isInternetConnected()
    }
}
