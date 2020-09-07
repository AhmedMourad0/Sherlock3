package dev.ahmedmourad.sherlock.domain.interactors.common

import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import io.reactivex.Flowable
import javax.inject.Inject

fun interface ObserveInternetConnectivityInteractor :
        () -> Flowable<Either<ObserveInternetConnectivityInteractor.Exception, Boolean>> {
    sealed class Exception {
        data class UnknownException(val origin: Throwable) : Exception()
    }
}

private fun ConnectivityManager.ObserveInternetConnectivityException.map() = when (this) {
    is ConnectivityManager.ObserveInternetConnectivityException.UnknownException ->
        ObserveInternetConnectivityInteractor.Exception.UnknownException(this.origin)
}

@Reusable
internal class ObserveInternetConnectivityInteractorImpl @Inject constructor(
        private val connectivityManager: Lazy<ConnectivityManager>
) : ObserveInternetConnectivityInteractor {
    override fun invoke(): Flowable<Either<ObserveInternetConnectivityInteractor.Exception, Boolean>> {
        return connectivityManager.get()
                .observeInternetConnectivity()
                .map { either ->
                    either.mapLeft(ConnectivityManager.ObserveInternetConnectivityException::map)
                }
    }
}
