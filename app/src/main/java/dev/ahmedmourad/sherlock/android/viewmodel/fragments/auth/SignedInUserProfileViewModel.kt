package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.left
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class SignedInUserProfileViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: Lazy<ObserveSignedInUserInteractor>
) : ViewModel() {

    val signedInUser by lazy {
        interactor.get()
                .invoke()
                .onErrorReturn { it.left() }
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    @Reusable
    class Factory @Inject constructor(
            private val observeSignedInUserInteractor: Provider<Lazy<ObserveSignedInUserInteractor>>
    ) : AssistedViewModelFactory<SignedInUserProfileViewModel> {
        override fun invoke(handle: SavedStateHandle): SignedInUserProfileViewModel {
            return SignedInUserProfileViewModel(
                    handle,
                    observeSignedInUserInteractor.get()
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
