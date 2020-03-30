package inc.ahmedmourad.sherlock.platform.managers

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import splitties.init.appCtx

internal class AndroidConnectivityManager : ConnectivityManager {

    override fun isInternetConnected(): Single<Boolean> {
        return ReactiveNetwork.checkInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    override fun observeInternetConnectivity(): Flowable<Boolean> {
        return ReactiveNetwork.observeNetworkConnectivity(appCtx)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMapSingle { ReactiveNetwork.checkInternetConnectivity() }
                .toFlowable(BackpressureStrategy.LATEST)
    }
}
