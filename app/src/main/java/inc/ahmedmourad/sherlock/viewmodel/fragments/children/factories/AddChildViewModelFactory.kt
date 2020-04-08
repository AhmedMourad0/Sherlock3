package inc.ahmedmourad.sherlock.viewmodel.fragments.children.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.dagger.modules.factories.SherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.viewmodel.fragments.children.AddChildViewModel

internal class AddChildViewModelFactory(
        private val serviceFactory: SherlockServiceIntentFactory,
        private val observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = AddChildViewModel(
            serviceFactory,
            observeChildPublishingStateInteractor
    ) as T
}
