package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.left
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Provider

internal class ChildDetailsViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: FindChildInteractor
) : ViewModel() {

    private val childId: ChildId =
            savedStateHandle.get<Bundle>(KEY_CHILD_ID)!!.unbundle(ChildId.serializer())

    private val refreshSubject = PublishSubject.create<Unit>()

    val result: LiveData<Either<Throwable, Tuple2<RetrievedChild, Weight?>?>> = interactor(childId)
            .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
            .onErrorReturn { it.left() }
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()

    fun onRefresh() = refreshSubject.onNext(Unit)

    internal class Factory @Inject constructor(
            private val interactor: Provider<FindChildInteractor>
    ) : AssistedViewModelFactory<ChildDetailsViewModel> {
        override fun invoke(handle: SavedStateHandle): ChildDetailsViewModel {
            return ChildDetailsViewModel(handle, interactor.get())
        }
    }

    companion object {

        private const val KEY_CHILD_ID =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.key.CHILD_ID"

        fun defaultArgs(childId: ChildId): Bundle? {
            return Bundle(1).apply {
                putBundle(KEY_CHILD_ID, childId.bundle(ChildId.serializer()))
            }
        }
    }
}
