package dev.ahmedmourad.sherlock.android.viewmodel.shared

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class GlobalViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        observeInternetConnectivityInteractor: Lazy<ObserveInternetConnectivityInteractor>,
        observeSignedInUserInteractor: Lazy<ObserveSignedInUserInteractor>
) : ViewModel() {

    private val refreshSubject = PublishSubject.create<Unit>()

    val internetConnectivityState by lazy {
        Flowable.just<InternetConnectivityState>(InternetConnectivityState.Loading)
                .concatWith(observeInternetConnectivityInteractor.get()
                        .invoke()
                        .retry()
                        .map { resultsEither ->
                            resultsEither.fold<InternetConnectivityState>(ifLeft = { e ->
                                when (e) {
                                    is ObserveInternetConnectivityInteractor.Exception.UnknownException -> {
                                        Timber.error(e.origin, e::toString)
                                        InternetConnectivityState.Error
                                    }
                                }
                            }, ifRight = { isConnected ->
                                if (isConnected) {
                                    InternetConnectivityState.Connected
                                } else {
                                    InternetConnectivityState.Disconnected
                                }
                            })
                        }
                ).observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    val userState by lazy {
        Flowable.just<UserState>(UserState.Loading).concatWith(observeSignedInUserInteractor.get()
                .invoke()
                .map { resultsEither ->
                    resultsEither.fold<UserState>(ifLeft = { e ->
                        when (e) {

                            ObserveSignedInUserInteractor.Exception.NoInternetConnectionException -> {
                                UserState.NoInternet
                            }

                            is ObserveSignedInUserInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e::toString)
                                UserState.Error
                            }

                            is ObserveSignedInUserInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e::toString)
                                UserState.Error
                            }
                        }
                    }, ifRight = { user ->
                        user?.fold(ifLeft = {
                            UserState.Incomplete(it)
                        }, ifRight = {
                            UserState.Authenticated(it)
                        }) ?: UserState.Unauthenticated
                    })
                }
        ).retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    val signedInUserSimplified: SignedInUser?
        get() {
            val state = userState.value
            return if (state is UserState.Authenticated) {
                state.user
            } else {
                null
            }
        }

    fun onRefresh() = refreshSubject.onNext(Unit)

    sealed class InternetConnectivityState {
        object Connected : InternetConnectivityState()
        object Disconnected : InternetConnectivityState()
        object Loading : InternetConnectivityState()
        object Error : InternetConnectivityState()
    }

    sealed class UserState {
        data class Authenticated(val user: SignedInUser) : UserState()
        data class Incomplete(val user: IncompleteUser) : UserState()
        object Unauthenticated : UserState()
        object Loading : UserState()
        object NoInternet : UserState()
        object Error : UserState()
    }

    @Reusable
    class Factory @Inject constructor(
            private val observeInternetConnectivityInteractor: Provider<Lazy<ObserveInternetConnectivityInteractor>>,
            private val observeSignedInUserInteractor: Provider<Lazy<ObserveSignedInUserInteractor>>
    ) : AssistedViewModelFactory<GlobalViewModel> {
        override fun invoke(handle: SavedStateHandle): GlobalViewModel {
            return GlobalViewModel(
                    handle,
                    observeInternetConnectivityInteractor.get(),
                    observeSignedInUserInteractor.get()
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
