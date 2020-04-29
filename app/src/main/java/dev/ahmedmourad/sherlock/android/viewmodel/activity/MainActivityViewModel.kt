package dev.ahmedmourad.sherlock.android.viewmodel.activity

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class MainActivityViewModel(
        private val savedStateHandle: SavedStateHandle,
        signOutInteractor: SignOutInteractor
) : ViewModel() {

    val isInPrimaryContentMode: LiveData<Boolean>
            by lazy { savedStateHandle.getLiveData<Boolean>(KEY_IS_IN_PRIMARY_MODE, null) }

    fun onIsInPrimaryModeChange(newValue: Boolean) {
        if (newValue != isInPrimaryContentMode.value) {
            savedStateHandle.set(KEY_IS_IN_PRIMARY_MODE, newValue)
        }
    }

    val signOutSingle = signOutInteractor().observeOn(AndroidSchedulers.mainThread())

    @Reusable
    class Factory @Inject constructor(
            private val signOutInteractor: Provider<SignOutInteractor>
    ) : AssistedViewModelFactory<MainActivityViewModel> {
        override fun invoke(handle: SavedStateHandle): MainActivityViewModel {
            return MainActivityViewModel(
                    handle,
                    signOutInteractor.get()
            )
        }
    }

    companion object {

        private const val KEY_IS_IN_PRIMARY_MODE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.activity.IS_IN_PRIMARY_MODE"

        fun defaultArgs(isInPrimaryMode: Boolean): Bundle? {
            return Bundle(1).apply {
                putBoolean(KEY_IS_IN_PRIMARY_MODE, isInPrimaryMode)
            }
        }
    }
}
