package inc.ahmedmourad.sherlock.viewmodel.factory

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.savedstate.SavedStateRegistryOwner

internal typealias SimpleViewModelFactoryFactory =
        (@JvmSuppressWildcards SavedStateRegistryOwner) -> @JvmSuppressWildcards AbstractSavedStateViewModelFactory
