package inc.ahmedmourad.sherlock.viewmodel.fragments.children.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.FindChildrenViewModel

internal class FindChildrenViewModelFactory(
        private val observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = FindChildrenViewModel(
            observeInternetConnectivityInteractor
    ) as T
}
