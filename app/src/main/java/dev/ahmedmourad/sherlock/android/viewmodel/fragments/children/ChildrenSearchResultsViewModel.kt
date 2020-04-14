package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.left
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

internal class ChildrenSearchResultsViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: FindChildrenInteractor,
        filterFactory: ChildrenFilterFactory,
        query: ChildQuery
) : ViewModel() {

    private val refreshSubject = PublishSubject.create<Unit>()

    val searchResults: LiveData<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> =
            interactor(query, filterFactory(query))
                    .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                    .onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    fun onRefresh() = refreshSubject.onNext(Unit)

    class Factory(
            owner: SavedStateRegistryOwner,
            private val interactor: FindChildrenInteractor,
            private val filterFactory: ChildrenFilterFactory,
            private val query: ChildQuery
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return ChildrenSearchResultsViewModel(handle, interactor, filterFactory, query) as T
        }
    }
}

internal typealias ChildrenSearchResultsViewModelFactoryFactory =
        (@JvmSuppressWildcards SavedStateRegistryOwner, @JvmSuppressWildcards ChildQuery) ->
        @JvmSuppressWildcards AbstractSavedStateViewModelFactory
