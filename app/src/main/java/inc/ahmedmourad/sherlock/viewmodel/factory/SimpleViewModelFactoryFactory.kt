package inc.ahmedmourad.sherlock.viewmodel.factory

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.savedstate.SavedStateRegistryOwner

internal typealias SimpleViewModelFactoryFactory =
        (@JvmSuppressWildcards SavedStateRegistryOwner, @JvmSuppressWildcards Bundle?) ->
        @JvmSuppressWildcards AbstractSavedStateViewModelFactory
