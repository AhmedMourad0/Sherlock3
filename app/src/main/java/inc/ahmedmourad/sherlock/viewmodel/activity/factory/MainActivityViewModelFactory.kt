package inc.ahmedmourad.sherlock.viewmodel.activity.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.FindSignedInUserInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveUserAuthStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.viewmodel.activity.MainActivityViewModel

internal class MainActivityViewModelFactory(
        private val observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        private val observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
        private val findSignedInUserInteractor: FindSignedInUserInteractor,
        private val signOutInteractor: SignOutInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = MainActivityViewModel(
            observeInternetConnectivityInteractor,
            observeUserAuthStateInteractor,
            findSignedInUserInteractor,
            signOutInteractor
    ) as T
}
