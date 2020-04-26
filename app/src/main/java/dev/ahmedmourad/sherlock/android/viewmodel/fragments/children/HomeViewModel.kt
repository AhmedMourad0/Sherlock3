package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import javax.inject.Inject

internal class HomeViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle
) : ViewModel() {

    @Reusable
    class Factory @Inject constructor() : AssistedViewModelFactory<HomeViewModel> {
        override fun invoke(handle: SavedStateHandle): HomeViewModel {
            return HomeViewModel(handle)
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
