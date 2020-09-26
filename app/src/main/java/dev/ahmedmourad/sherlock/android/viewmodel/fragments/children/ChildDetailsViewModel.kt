package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.toPair
import dagger.Lazy
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildInteractor
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class ChildDetailsViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: Lazy<FindChildInteractor>
) : ViewModel() {

    private val childId: ChildId =
            savedStateHandle.get<Bundle>(KEY_CHILD_ID)!!.unbundle(ChildId.serializer())

    private val refreshSubject = PublishSubject.create<Unit>()

    val state by lazy {
        Flowable.just<State>(State.Loading)
                .concatWith(interactor.get()
                        .invoke(childId)
                        .map { resultsEither ->
                            resultsEither.fold<State>(ifLeft = { e ->
                                when (e) {

                                    FindChildInteractor.Exception.NoInternetConnectionException -> {
                                        State.NoInternet
                                    }

                                    FindChildInteractor.Exception.NoSignedInUserException -> {
                                        State.NoSignedInUser
                                    }

                                    is FindChildInteractor.Exception.InternalException -> {
                                        Timber.error(e.origin, e::toString)
                                        State.Error
                                    }

                                    is FindChildInteractor.Exception.UnknownException -> {
                                        Timber.error(e.origin, e::toString)
                                        State.Error
                                    }
                                }
                            }, ifRight = { tuple ->
                                if (tuple == null) {
                                    State.NoData
                                } else {
                                    State.Data(tuple.toPair())
                                }
                            })
                        }
                ).retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    fun onRefresh() = refreshSubject.onNext(Unit)

    sealed class State {
        data class Data(val item: Pair<RetrievedChild, Weight?>) : State()
        object NoData : State()
        object Loading : State()
        object NoInternet : State()
        object NoSignedInUser : State()
        object Error : State()
    }

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
