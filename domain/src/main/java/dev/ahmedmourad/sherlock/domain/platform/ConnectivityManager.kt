package dev.ahmedmourad.sherlock.domain.platform

import arrow.core.Either
import io.reactivex.Flowable
import io.reactivex.Single

interface ConnectivityManager {

    fun isInternetConnected(): Single<Either<IsInternetConnectedException, Boolean>>

    fun observeInternetConnectivity(): Flowable<Either<ObserveInternetConnectivityException, Boolean>>

    sealed class IsInternetConnectedException {
        data class UnknownException(val origin: Throwable) : IsInternetConnectedException()
    }

    sealed class ObserveInternetConnectivityException {
        data class UnknownException(val origin: Throwable) : ObserveInternetConnectivityException()
    }
}
