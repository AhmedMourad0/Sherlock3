package dev.ahmedmourad.sherlock.domain.platform

import io.reactivex.Flowable
import io.reactivex.Single

interface ConnectivityManager {

    fun isInternetConnected(): Single<Boolean>

    fun observeInternetConnectivity(): Flowable<Boolean>
}
