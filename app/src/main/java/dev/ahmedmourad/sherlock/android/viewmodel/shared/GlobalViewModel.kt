package dev.ahmedmourad.sherlock.android.viewmodel.shared

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveUserAuthStateInteractor
import dev.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

internal class GlobalViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
        observeSignedInUserInteractor: ObserveSignedInUserInteractor
) : ViewModel() {

    val internetConnectivity: LiveData<Either<Throwable, Boolean>> =
            observeInternetConnectivityInteractor()
                    .retry()
                    .map<Either<Throwable, Boolean>> { it.right() }
                    .onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    val userAuthState: LiveData<Either<Throwable, Boolean>> = observeUserAuthStateInteractor()
            .map<Either<Throwable, Boolean>> { it.right() }
            .onErrorReturn { it.left() }
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()

    val signedInUser: LiveData<Either<Throwable, Either<IncompleteUser, SignedInUser>>> =
            observeSignedInUserInteractor()
                    .onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    @Reusable
    class Factory @Inject constructor(
            private val observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
            private val observeUserAuthStateInteractor: ObserveUserAuthStateInteractor,
            private val observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ) : AssistedViewModelFactory<GlobalViewModel> {
        override fun invoke(handle: SavedStateHandle): GlobalViewModel {
            return GlobalViewModel(
                    handle,
                    observeInternetConnectivityInteractor,
                    observeUserAuthStateInteractor,
                    observeSignedInUserInteractor
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
