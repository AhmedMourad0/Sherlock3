package inc.ahmedmourad.sherlock.viewmodel.activity

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import io.reactivex.android.schedulers.AndroidSchedulers

internal class MainActivityViewModel(
        savedStateHandle: SavedStateHandle,
        signOutInteractor: SignOutInteractor
) : ViewModel() {

    val signOutSingle = signOutInteractor().observeOn(AndroidSchedulers.mainThread())

    class Factory(
            owner: SavedStateRegistryOwner,
            private val signOutInteractor: SignOutInteractor
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return MainActivityViewModel(
                    handle,
                    signOutInteractor
            ) as T
        }
    }
}
