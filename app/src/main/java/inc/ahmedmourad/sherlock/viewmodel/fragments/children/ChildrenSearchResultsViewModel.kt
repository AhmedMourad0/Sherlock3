package inc.ahmedmourad.sherlock.viewmodel.fragments.children

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.left
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.utils.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

internal class ChildrenSearchResultsViewModel(
        interactor: FindChildrenInteractor,
        filterFactory: ChildrenFilterFactory,
        query: ChildQuery
) : ViewModel() {

    private val refreshSubject = PublishSubject.create<Unit>()

    val searchResults: LiveData<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight>>>> =
            interactor(query, filterFactory(query))
                    .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                    .onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    fun onRefresh() = refreshSubject.onNext(Unit)

    class Factory(
            private val interactor: FindChildrenInteractor,
            private val filterFactory: ChildrenFilterFactory,
            private val query: ChildQuery
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChildrenSearchResultsViewModel(interactor, filterFactory, query) as T
        }
    }
}

internal typealias ChildrenSearchResultsViewModelFactoryFactory =
        (@JvmSuppressWildcards ChildQuery) -> @JvmSuppressWildcards ViewModelProvider.NewInstanceFactory
