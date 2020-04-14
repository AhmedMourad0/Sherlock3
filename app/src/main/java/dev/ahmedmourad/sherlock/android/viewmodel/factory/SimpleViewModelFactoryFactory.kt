package dev.ahmedmourad.sherlock.android.viewmodel.factory

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.savedstate.SavedStateRegistryOwner

internal typealias SimpleViewModelFactoryFactory =
        (@JvmSuppressWildcards SavedStateRegistryOwner) -> @JvmSuppressWildcards AbstractSavedStateViewModelFactory
