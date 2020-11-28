package dev.ahmedmourad.sherlock.auth.fakes

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Flowable
import io.reactivex.Single

internal class FakeConnectivityManager : ConnectivityManager {

    var hasInternet = true
    var triggerUnknownException = false

    override fun isInternetConnected():
            Single<Either<ConnectivityManager.IsInternetConnectedException, Boolean>> {
        return Single.defer {
            if (triggerUnknownException) {
                Single.just(ConnectivityManager.IsInternetConnectedException.UnknownException(RuntimeException()).left())
            } else {
                Single.just(hasInternet.right())
            }
        }
    }

    override fun observeInternetConnectivity():
            Flowable<Either<ConnectivityManager.ObserveInternetConnectivityException, Boolean>> {
        return Flowable.defer {
            if (triggerUnknownException) {
                Flowable.just(ConnectivityManager.ObserveInternetConnectivityException.UnknownException(RuntimeException()).left())
            } else {
                Flowable.just(hasInternet.right())
            }
        }
    }
}
