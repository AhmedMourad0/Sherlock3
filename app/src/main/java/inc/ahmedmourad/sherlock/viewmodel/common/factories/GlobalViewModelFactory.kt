package inc.ahmedmourad.sherlock.viewmodel.common.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveUserAuthStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.viewmodel.common.GlobalViewModel

internal class GlobalViewModelFactory(
        private val observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        private val observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
        private val observeSignedInUserInteractor: ObserveSignedInUserInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = GlobalViewModel(
            observeInternetConnectivityInteractor,
            observeUserAuthStateInteractor,
            observeSignedInUserInteractor
    ) as T
}
