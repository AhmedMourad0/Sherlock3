package inc.ahmedmourad.sherlock.viewmodel.controllers.children.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery

import inc.ahmedmourad.sherlock.viewmodel.controllers.children.ChildrenSearchResultsViewModel

internal typealias ChildrenSearchResultsViewModelFactoryFactory =
        (@JvmSuppressWildcards ChildQuery) -> @JvmSuppressWildcards ViewModelProvider.NewInstanceFactory

internal fun childrenSearchResultsViewModelFactoryFactory(
        interactor: FindChildrenInteractor,
        filterFactory: ChildrenFilterFactory,
        query: ChildQuery
): ChildrenSearchResultsViewModelFactory {
    return ChildrenSearchResultsViewModelFactory(interactor, filterFactory, query)
}

internal class ChildrenSearchResultsViewModelFactory(
        private val interactor: FindChildrenInteractor,
        private val filterFactory: ChildrenFilterFactory,
        private val query: ChildQuery
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChildrenSearchResultsViewModel(interactor, filterFactory, query) as T
    }
}
