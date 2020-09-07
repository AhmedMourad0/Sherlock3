package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.children.FindAllInvestigationsInteractor
import dev.ahmedmourad.sherlock.domain.model.children.Investigation
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class OngoingInvestigationsViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: Lazy<FindAllInvestigationsInteractor>
) : ViewModel() {

    private val refreshSubject = PublishSubject.create<Unit>()

    val state: LiveData<State> by lazy {
        Flowable.just<State>(State.Loading)
                .concatWith(interactor.get()
                        .invoke()
                        .map { investigationEither ->
                            investigationEither.fold<State>(ifLeft = { e ->
                                when (e) {

                                    FindAllInvestigationsInteractor.Exception.NoInternetConnectionException -> {
                                        State.NoInternet
                                    }

                                    FindAllInvestigationsInteractor.Exception.NoSignedInUserException -> {
                                        State.NoSignedInUser
                                    }

                                    is FindAllInvestigationsInteractor.Exception.InternalException -> {
                                        Timber.error(message = e::toString)
                                        State.Error
                                    }

                                    is FindAllInvestigationsInteractor.Exception.UnknownException -> {
                                        Timber.error(message = e::toString)
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

    fun onInvestigationSelected(investigation: Investigation) {
        Toast.makeText(appCtx, R.string.coming_soon, Toast.LENGTH_LONG).show()
    }

    sealed class State {
        data class Data(val items: List<Investigation>) : State()
        object NoData : State()
        object Loading : State()
        object NoInternet : State()
        object NoSignedInUser : State()
        object Error : State()
    }

    @Reusable
    class Factory @Inject constructor(
            private val interactor: Provider<Lazy<FindAllInvestigationsInteractor>>
    ) : AssistedViewModelFactory<OngoingInvestigationsViewModel> {
        override fun invoke(handle: SavedStateHandle): OngoingInvestigationsViewModel {
            return OngoingInvestigationsViewModel(handle, interactor.get())
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
