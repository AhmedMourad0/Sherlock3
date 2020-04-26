package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.left
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

internal class SignedInUserProfileViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,
        interactor: ObserveSignedInUserInteractor
) : ViewModel() {

    val signedInUser: LiveData<Either<Throwable, Either<IncompleteUser, SignedInUser>>> =
            interactor().onErrorReturn { it.left() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toLiveData()

    @Reusable
    class Factory @Inject constructor(
            private val observeSignedInUserInteractor: ObserveSignedInUserInteractor
    ) : AssistedViewModelFactory<SignedInUserProfileViewModel> {
        override fun invoke(handle: SavedStateHandle): SignedInUserProfileViewModel {
            return SignedInUserProfileViewModel(
                    handle,
                    observeSignedInUserInteractor
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
