package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Flowable

typealias ObserveInternetConnectivityInteractor =
        () -> @JvmSuppressWildcards Flowable<Boolean>

internal class ObserveInternetConnectivityInteractorImpl(
        private val connectivityManager: Lazy<ConnectivityManager>
) : ObserveInternetConnectivityInteractor {
    override fun invoke(): Flowable<Boolean> {
        return connectivityManager.get().observeInternetConnectivity()
    }
}
