package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.filter.ChildrenFilterFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Provider

internal class ChildrenSearchResultsViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: Lazy<FindChildrenInteractor>,
        filterFactory: Lazy<ChildrenFilterFactory>
) : ViewModel() {

    private val query: ChildQuery =
            savedStateHandle.get<Bundle>(KEY_CHILD_QUERY)!!.unbundle(ChildQuery.serializer())

    private val refreshSubject = PublishSubject.create<Unit>()

    val searchResults by lazy {
        interactor.get()
                .invoke(query, filterFactory.get().invoke(query))
                .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    fun onRefresh() = refreshSubject.onNext(Unit)

    @Reusable
    class Factory @Inject constructor(
            private val interactor: Provider<Lazy<FindChildrenInteractor>>,
            private val filterFactory: Provider<Lazy<ChildrenFilterFactory>>
    ) : AssistedViewModelFactory<ChildrenSearchResultsViewModel> {
        override fun invoke(handle: SavedStateHandle): ChildrenSearchResultsViewModel {
            return ChildrenSearchResultsViewModel(handle, interactor.get(), filterFactory.get())
        }
    }

    companion object {

        private const val KEY_CHILD_QUERY =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.key.CHILD_QUERY"

        fun defaultArgs(query: ChildQuery): Bundle? {
            return Bundle(1).apply {
                putBundle(KEY_CHILD_QUERY, query.bundle(ChildQuery.serializer()))
            }
        }
    }
}
