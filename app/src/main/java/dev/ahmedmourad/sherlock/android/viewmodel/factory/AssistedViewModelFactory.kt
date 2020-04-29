package dev.ahmedmourad.sherlock.android.viewmodel.factory

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Provider

internal interface AssistedViewModelFactory<out T : ViewModel> : (SavedStateHandle) -> T

internal class SimpleSavedStateViewModelFactory<out VM : ViewModel>(
        owner: SavedStateRegistryOwner,
        private val viewModelFactory: Provider<AssistedViewModelFactory<VM>>,
        defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
    ): T {
        return viewModelFactory.get()(handle) as T
    }
}
