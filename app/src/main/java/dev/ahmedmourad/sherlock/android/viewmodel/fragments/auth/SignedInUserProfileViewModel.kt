package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.left
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SignedInUserProfileViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: ObserveSignedInUserInteractor
) : ViewModel() {

    val signedInUser: LiveData<Either<Throwable, Either<IncompleteUser, SignedInUser>>> =
            interactor().onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    class Factory(
            owner: SavedStateRegistryOwner,
            private val observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return SignedInUserProfileViewModel(
                    handle,
                    observeSignedInUserInteractor
            ) as T

        }
    }
}
