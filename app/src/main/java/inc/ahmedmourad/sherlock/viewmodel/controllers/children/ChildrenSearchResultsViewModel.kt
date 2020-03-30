package inc.ahmedmourad.sherlock.viewmodel.controllers.children

import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.Tuple2
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

internal class ChildrenSearchResultsViewModel(
        interactor: FindChildrenInteractor,
        filterFactory: ChildrenFilterFactory,
        query: ChildQuery
) : ViewModel() {

    private val refreshSubject = PublishSubject.create<Unit>()

    val searchResultsFlowable: Flowable<Either<Throwable, List<Tuple2<SimpleRetrievedChild, Weight>>>>

    init {
        searchResultsFlowable = interactor(query, filterFactory(query))
                .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onRefresh() = refreshSubject.onNext(Unit)
}
