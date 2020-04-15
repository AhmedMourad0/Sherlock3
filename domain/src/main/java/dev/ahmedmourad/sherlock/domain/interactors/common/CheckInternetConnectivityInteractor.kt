package dev.ahmedmourad.sherlock.domain.interactors.common

import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Single
import javax.inject.Inject

typealias CheckInternetConnectivityInteractor =
        () -> @JvmSuppressWildcards Single<Boolean>

@Reusable
internal class CheckInternetConnectivityInteractorImpl @Inject constructor(
        private val connectivityManager: Lazy<ConnectivityManager>
) : CheckInternetConnectivityInteractor {
    override fun invoke(): Single<Boolean> {
        return connectivityManager.get().isInternetConnected()
    }
}
