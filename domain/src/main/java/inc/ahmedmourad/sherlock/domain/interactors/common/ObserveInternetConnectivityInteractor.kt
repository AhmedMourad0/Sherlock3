package inc.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Flowable

typealias ObserveInternetConnectivityInteractor = () -> @JvmSuppressWildcards Flowable<Boolean>

internal fun observeInternetConnectivity(connectivityManager: Lazy<ConnectivityManager>): Flowable<Boolean> {
    return connectivityManager.get().observeInternetConnectivity()
}
