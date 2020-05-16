package dev.ahmedmourad.sherlock.android.viewmodel.shared

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveUserAuthStateInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class GlobalViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        observeInternetConnectivityInteractor: Lazy<ObserveInternetConnectivityInteractor>,
        observeUserAuthStateInteractor: Lazy<ObserveUserAuthStateInteractor>,
        observeSignedInUserInteractor: Lazy<ObserveSignedInUserInteractor>
) : ViewModel() {

    val internetConnectivity by lazy {
        observeInternetConnectivityInteractor.get()
                .invoke()
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    val userAuthState by lazy {
        observeUserAuthStateInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    val signedInUser by lazy {
        observeSignedInUserInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    @Reusable
    class Factory @Inject constructor(
            private val observeInternetConnectivityInteractor: Provider<Lazy<ObserveInternetConnectivityInteractor>>,
            private val observeUserAuthStateInteractor: Provider<Lazy<ObserveUserAuthStateInteractor>>,
            private val observeSignedInUserInteractor: Provider<Lazy<ObserveSignedInUserInteractor>>
    ) : AssistedViewModelFactory<GlobalViewModel> {
        override fun invoke(handle: SavedStateHandle): GlobalViewModel {
            return GlobalViewModel(
                    handle,
                    observeInternetConnectivityInteractor.get(),
                    observeUserAuthStateInteractor.get(),
                    observeSignedInUserInteractor.get()
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
