package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.AddInvestigationInteractor
import dev.ahmedmourad.sherlock.domain.interactors.children.FindChildrenInteractor
import dev.ahmedmourad.sherlock.domain.interactors.children.InvalidateAllQueriesInteractor
import dev.ahmedmourad.sherlock.domain.model.children.ChildrenQuery
import dev.ahmedmourad.sherlock.domain.model.children.Investigation
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class ChildrenSearchResultsViewModel(
        private val savedStateHandle: SavedStateHandle,
        findChildrenInteractor: Lazy<FindChildrenInteractor>,
        private val addInvestigationInteractor: Lazy<AddInvestigationInteractor>,
        private val invalidateAllQueriesInteractor: Lazy<InvalidateAllQueriesInteractor>
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val query: ChildrenQuery =
            savedStateHandle.get<Bundle>(KEY_CHILD_QUERY)!!.unbundle(ChildrenQuery.serializer())

    val stateInvestigationState: LiveData<StartInvestigationState?> by lazy {
        Transformations.map(savedStateHandle.getLiveData<Bundle?>(
                KEY_START_INVESTIGATION_STATE,
                null
        )) {
            it?.unbundle(StartInvestigationState.serializer())
        }
    }

    fun onStartInvestigationStateHandled() {
        savedStateHandle.set(KEY_START_INVESTIGATION_STATE, null)
    }

    private val refreshSubject = PublishSubject.create<Unit>()

    val state by lazy {
        Flowable.just<State>(State.Loading)
                .concatWith(findChildrenInteractor.get()
                        .invoke(query)
                        .map { resultsEither ->
                            resultsEither.fold<State>(ifLeft = { e ->
                                when (e) {

                                    FindChildrenInteractor.Exception.NoInternetConnectionException -> {
                                        State.NoInternet
                                    }

                                    FindChildrenInteractor.Exception.NoSignedInUserException -> {
                                        State.NoSignedInUser
                                    }

                                    is FindChildrenInteractor.Exception.UnknownException -> {
                                        Timber.error(e.origin, e::toString)
                                        State.Error
                                    }
                                }
                            }, ifRight = { list ->
                                if (list.isEmpty()) {
                                    State.NoData
                                } else {
                                    State.Data(list)
                                }
                            })
                        }
                ).retryWhen { refreshSubject.toFlowable(BackpressureStrategy.LATEST) }
                .observeOn(AndroidSchedulers.mainThread())
                .toLiveData()
    }

    fun onRefresh() = refreshSubject.onNext(Unit)

    fun onStartInvestigation() {
        compositeDisposable.add(addInvestigationInteractor.get()
                .invoke(query.toInvestigation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->
                    either.fold(ifLeft = { e ->
                        when (e) {

                            AddInvestigationInteractor.Exception.NoInternetConnectionException -> {
                                savedStateHandle.set(
                                        KEY_START_INVESTIGATION_STATE,
                                        StartInvestigationState.NoInternet.bundle(StartInvestigationState.serializer())
                                )
                            }

                            AddInvestigationInteractor.Exception.NoSignedInUserException -> {
                                savedStateHandle.set(
                                        KEY_START_INVESTIGATION_STATE,
                                        StartInvestigationState.NoSignedInUser.bundle(StartInvestigationState.serializer())
                                )
                            }

                            is AddInvestigationInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_START_INVESTIGATION_STATE,
                                        StartInvestigationState.Error.bundle(StartInvestigationState.serializer())
                                )
                            }
                        }.exhaust()
                    }, ifRight = {
                        savedStateHandle.set(
                                KEY_START_INVESTIGATION_STATE,
                                StartInvestigationState.Success(it).bundle(StartInvestigationState.serializer())
                        )
                    })
                }, {
                    Timber.error(it, it::toString)
                    savedStateHandle.set(
                            KEY_START_INVESTIGATION_STATE,
                            StartInvestigationState.Error.bundle(StartInvestigationState.serializer())
                    )
                }))
    }

    fun onInvalidateAllQueries() {
        compositeDisposable.add(invalidateAllQueriesInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }, {
                    Timber.error(it, it::toString)
                })
        )
    }

    override fun onCleared() {
        compositeDisposable.add(invalidateAllQueriesInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    compositeDisposable.dispose()
                }, {
                    Timber.error(it, it::toString)
                    compositeDisposable.dispose()
                })
        )
        super.onCleared()
    }

    @Serializable
    sealed class StartInvestigationState {

        @Serializable
        data class Success(val item: Investigation) : StartInvestigationState()

        @Serializable
        object Loading : StartInvestigationState()

        @Serializable
        object NoInternet : StartInvestigationState()

        @Serializable
        object NoSignedInUser : StartInvestigationState()

        @Serializable
        object Error : StartInvestigationState()
    }

    sealed class State {
        data class Data(val items: Map<SimpleRetrievedChild, Weight>) : State()
        object NoData : State()
        object Loading : State()
        object NoInternet : State()
        object NoSignedInUser : State()
        object Error : State()
    }

    @Reusable
    class Factory @Inject constructor(
            private val findChildrenInteractor: Provider<Lazy<FindChildrenInteractor>>,
            private val addInvestigationInteractor: Provider<Lazy<AddInvestigationInteractor>>,
            private val invalidateAllQueriesInteractor: Provider<Lazy<InvalidateAllQueriesInteractor>>
    ) : AssistedViewModelFactory<ChildrenSearchResultsViewModel> {
        override fun invoke(handle: SavedStateHandle): ChildrenSearchResultsViewModel {
            return ChildrenSearchResultsViewModel(
                    handle,
                    findChildrenInteractor.get(),
                    addInvestigationInteractor.get(),
                    invalidateAllQueriesInteractor.get()
            )
        }
    }

    companion object {

        private const val KEY_CHILD_QUERY =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.key.CHILD_QUERY"

        private const val KEY_START_INVESTIGATION_STATE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.key.START_INVESTIGATION_STATE"

        fun defaultArgs(query: ChildrenQuery): Bundle? {
            return Bundle(1).apply {
                putBundle(KEY_CHILD_QUERY, query.bundle(ChildrenQuery.serializer()))
            }
        }
    }
}
