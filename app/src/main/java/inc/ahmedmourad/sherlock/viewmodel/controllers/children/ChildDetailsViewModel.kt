package inc.ahmedmourad.sherlock.viewmodel.controllers.children

import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.Tuple2
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

internal class ChildDetailsViewModel(child: SimpleRetrievedChild, interactor: FindChildInteractor) : ViewModel() {

    private val refreshSubject = PublishSubject.create<Unit>()

    val result: Flowable<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>>

    init {
        result = interactor(child)
                .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onRefresh() = refreshSubject.onNext(Unit)
}
