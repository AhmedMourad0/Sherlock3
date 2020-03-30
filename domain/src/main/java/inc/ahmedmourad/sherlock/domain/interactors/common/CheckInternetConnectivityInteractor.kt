package inc.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Single

typealias CheckInternetConnectivityInteractor = () -> @JvmSuppressWildcards Single<Boolean>

internal fun checkInternetConnectivity(connectivityManager: Lazy<ConnectivityManager>): Single<Boolean> {
    return connectivityManager.get().isInternetConnected()
}
