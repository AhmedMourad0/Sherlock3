package inc.ahmedmourad.sherlock.viewmodel.fragments.children.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.dagger.modules.factories.SherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.domain.interactors.common.CheckChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.CheckInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.AddChildViewModel

internal class AddChildViewModelFactory(
        private val serviceFactory: SherlockServiceIntentFactory,
        private val observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        private val checkInternetConnectivityInteractor: CheckInternetConnectivityInteractor,
        private val observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor,
        private val checkChildPublishingStateInteractor: CheckChildPublishingStateInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = AddChildViewModel(
            serviceFactory,
            observeInternetConnectivityInteractor,
            checkInternetConnectivityInteractor,
            observeChildPublishingStateInteractor,
            checkChildPublishingStateInteractor
    ) as T
}
