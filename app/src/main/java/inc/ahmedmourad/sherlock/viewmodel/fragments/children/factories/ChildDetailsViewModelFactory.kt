package inc.ahmedmourad.sherlock.viewmodel.fragments.children.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId

import inc.ahmedmourad.sherlock.viewmodel.fragments.children.ChildDetailsViewModel

internal typealias ChildDetailsViewModelFactoryFactory =
        (@JvmSuppressWildcards ChildId) -> @JvmSuppressWildcards ViewModelProvider.NewInstanceFactory

internal fun childDetailsViewModelFactoryFactory(
        interactor: FindChildInteractor,
        childId: ChildId
): ChildDetailsViewModelFactory {
    return ChildDetailsViewModelFactory(childId, interactor)
}

internal class ChildDetailsViewModelFactory(
        private val childId: ChildId,
        private val interactor: FindChildInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ChildDetailsViewModel(childId, interactor) as T
}
