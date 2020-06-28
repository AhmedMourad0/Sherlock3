package dev.ahmedmourad.sherlock.platform.managers

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx
import javax.inject.Inject

@Reusable
internal class AndroidConnectivityManager @Inject constructor() : ConnectivityManager {

    override fun isInternetConnected():
            Single<Either<ConnectivityManager.IsInternetConnectedException, Boolean>> {
        return ReactiveNetwork.checkInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map<Either<ConnectivityManager.IsInternetConnectedException, Boolean>> {
                    it.right()
                }.onErrorReturn {
                    ConnectivityManager.IsInternetConnectedException.UnknownException(it).left()
                }
    }

    override fun observeInternetConnectivity():
            Flowable<Either<ConnectivityManager.ObserveInternetConnectivityException, Boolean>> {
        return ReactiveNetwork.observeNetworkConnectivity(appCtx)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .switchMapSingle { ReactiveNetwork.checkInternetConnectivity() }
                .toFlowable(BackpressureStrategy.LATEST)
                .map<Either<ConnectivityManager.ObserveInternetConnectivityException, Boolean>> {
                    it.right()
                }.onErrorReturn {
                    ConnectivityManager.ObserveInternetConnectivityException.UnknownException(it).left()
                }
    }
}
