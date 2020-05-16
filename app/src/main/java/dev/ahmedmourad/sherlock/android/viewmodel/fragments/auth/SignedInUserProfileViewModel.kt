package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import javax.inject.Inject

internal class SignedInUserProfileViewModel(
        @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle
) : ViewModel() {

    @Reusable
    class Factory @Inject constructor() : AssistedViewModelFactory<SignedInUserProfileViewModel> {
        override fun invoke(handle: SavedStateHandle): SignedInUserProfileViewModel {
            return SignedInUserProfileViewModel(
                    handle
            )
        }
    }

    companion object {
        fun defaultArgs(): Bundle? = null
    }
}
