package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.left
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.utils.toLiveData
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SignedInUserProfileViewModel(
        savedStateHandle: SavedStateHandle,
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
