package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.left
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.bundlizer.bundle
import dev.ahmedmourad.sherlock.android.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.filter.ChildrenFilterFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

internal class ChildrenSearchResultsViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: FindChildrenInteractor,
        filterFactory: ChildrenFilterFactory
) : ViewModel() {

    private val query: ChildQuery =
            savedStateHandle.get<Bundle>(KEY_CHILD_QUERY)!!.unbundle(ChildQuery.serializer())

    private val refreshSubject = PublishSubject.create<Unit>()

    val searchResults: LiveData<Either<Throwable, Map<SimpleRetrievedChild, Weight>>> =
            interactor(query, filterFactory(query))
                    .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                    .onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    fun onRefresh() = refreshSubject.onNext(Unit)

    @Reusable
    class Factory @Inject constructor(
            private val interactor: FindChildrenInteractor,
            private val filterFactory: ChildrenFilterFactory
    ) : AssistedViewModelFactory<ChildrenSearchResultsViewModel> {
        override fun invoke(handle: SavedStateHandle): ChildrenSearchResultsViewModel {
            return ChildrenSearchResultsViewModel(handle, interactor, filterFactory)
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
