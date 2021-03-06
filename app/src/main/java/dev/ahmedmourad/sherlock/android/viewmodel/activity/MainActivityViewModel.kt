package dev.ahmedmourad.sherlock.android.viewmodel.activity

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignOutInteractor
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class MainActivityViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val signOutInteractor: Lazy<SignOutInteractor>
) : ViewModel() {

    private var disposable by disposable()

    val isInPrimaryContentMode: LiveData<Boolean>
            by lazy { savedStateHandle.getLiveData<Boolean>(KEY_IS_IN_PRIMARY_MODE, null) }

    fun onIsInPrimaryModeChange(newValue: Boolean) {
        if (newValue != isInPrimaryContentMode.value) {
            savedStateHandle.set(KEY_IS_IN_PRIMARY_MODE, newValue)
        }
    }

    fun onSignOut() {
        disposable = signOutInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it is Either.Left) {
                        when (val e = it.a) {
                            SignOutInteractor.Exception.NoInternetConnectionException -> Unit
                            is SignOutInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e.origin::toString)
                            }
                            is SignOutInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                            }
                        }.exhaust()
                    }
                }, {
                    Timber.error(it, it::toString)
                })
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

    @Reusable
    class Factory @Inject constructor(
            private val signOutInteractor: Provider<Lazy<SignOutInteractor>>
    ) : AssistedViewModelFactory<MainActivityViewModel> {
        override fun invoke(handle: SavedStateHandle): MainActivityViewModel {
            return MainActivityViewModel(
                    handle,
                    signOutInteractor.get()
            )
        }
    }

    companion object {

        private const val KEY_IS_IN_PRIMARY_MODE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.activity.IS_IN_PRIMARY_MODE"

        fun defaultArgs(isInPrimaryMode: Boolean): Bundle? {
            return Bundle(1).apply {
                putBoolean(KEY_IS_IN_PRIMARY_MODE, isInPrimaryMode)
            }
        }
    }
}
