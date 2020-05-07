package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.left
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Provider

internal class ChildDetailsViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: Lazy<FindChildInteractor>
) : ViewModel() {

    private val childId: ChildId =
            savedStateHandle.get<Bundle>(KEY_CHILD_ID)!!.unbundle(ChildId.serializer())

    private val refreshSubject = PublishSubject.create<Unit>()

    val result by lazy {
        interactor.get()
                .invoke(childId)
                .retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                .onErrorReturn { it.left() }
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    fun onRefresh() = refreshSubject.onNext(Unit)

    internal class Factory @Inject constructor(
            private val interactor: Provider<Lazy<FindChildInteractor>>
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
