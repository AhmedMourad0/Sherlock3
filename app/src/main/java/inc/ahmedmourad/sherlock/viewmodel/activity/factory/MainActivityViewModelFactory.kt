package inc.ahmedmourad.sherlock.viewmodel.activity.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import inc.ahmedmourad.sherlock.viewmodel.activity.MainActivityViewModel

internal class MainActivityViewModelFactory(
        private val signOutInteractor: SignOutInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = MainActivityViewModel(
            signOutInteractor
    ) as T
}
