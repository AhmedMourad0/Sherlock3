package dev.ahmedmourad.sherlock.android.viewmodel.shared

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import arrow.core.orNull
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveCurrentUserInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveUserAuthStateInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class GlobalViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        observeInternetConnectivityInteractor: Lazy<ObserveInternetConnectivityInteractor>,
        observeUserAuthStateInteractor: Lazy<ObserveUserAuthStateInteractor>,
        observeCurrentUserInteractor: Lazy<ObserveCurrentUserInteractor>
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
        observeCurrentUserInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    val signedInUserSimplified: LiveData<SignedInUser?> = Transformations.map(signedInUser) {
        it.orNull()?.orNull()
    }

    @Reusable
    class Factory @Inject constructor(
            private val observeInternetConnectivityInteractor: Provider<Lazy<ObserveInternetConnectivityInteractor>>,
            private val observeUserAuthStateInteractor: Provider<Lazy<ObserveUserAuthStateInteractor>>,
            private val observeCurrentUserInteractor: Provider<Lazy<ObserveCurrentUserInteractor>>
    ) : AssistedViewModelFactory<GlobalViewModel> {
        override fun invoke(handle: SavedStateHandle): GlobalViewModel {
            return GlobalViewModel(
                    handle,
                    observeInternetConnectivityInteractor.get(),
                    observeUserAuthStateInteractor.get(),
                    observeCurrentUserInteractor.get()
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
