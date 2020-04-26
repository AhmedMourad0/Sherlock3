package dev.ahmedmourad.sherlock.android.viewmodel.activity

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

internal class MainActivityViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        signOutInteractor: SignOutInteractor
) : ViewModel() {

    val signOutSingle = signOutInteractor().observeOn(AndroidSchedulers.mainThread())

    @Reusable
    class Factory @Inject constructor(
            private val signOutInteractor: SignOutInteractor
    ) : AssistedViewModelFactory<MainActivityViewModel> {
        override fun invoke(handle: SavedStateHandle): MainActivityViewModel {
            return MainActivityViewModel(
                    handle,
                    signOutInteractor
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
