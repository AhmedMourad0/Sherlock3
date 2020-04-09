package inc.ahmedmourad.sherlock.viewmodel.fragments.children

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.left
import inc.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import inc.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId
import inc.ahmedmourad.sherlock.utils.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

internal class ChildDetailsViewModel(childId: ChildId, interactor: FindChildInteractor) : ViewModel() {

    private val refreshSubject = PublishSubject.create<Unit>()

    val result: LiveData<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>> = interactor(childId)
            .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
            .onErrorReturn { it.left() }
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()

    fun onRefresh() = refreshSubject.onNext(Unit)

    internal class Factory(
            private val childId: ChildId,
            private val interactor: FindChildInteractor
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChildDetailsViewModel(childId, interactor) as T
        }
    }
}

internal typealias ChildDetailsViewModelFactoryFactory =
        (@JvmSuppressWildcards ChildId) -> @JvmSuppressWildcards ViewModelProvider.NewInstanceFactory
