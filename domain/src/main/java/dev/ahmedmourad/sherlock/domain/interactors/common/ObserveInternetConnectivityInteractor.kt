package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Flowable
import javax.inject.Inject

typealias ObserveInternetConnectivityInteractor =
        () -> @JvmSuppressWildcards Flowable<Boolean>

@Reusable
internal class ObserveInternetConnectivityInteractorImpl @Inject constructor(
        private val connectivityManager: Lazy<ConnectivityManager>
) : ObserveInternetConnectivityInteractor {
    override fun invoke(): Flowable<Boolean> {
        return connectivityManager.get().observeInternetConnectivity()
    }
}
