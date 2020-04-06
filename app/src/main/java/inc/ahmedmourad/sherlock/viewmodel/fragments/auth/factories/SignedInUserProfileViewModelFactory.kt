package inc.ahmedmourad.sherlock.viewmodel.fragments.auth.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.ObserveSignedInUserInteractor
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.SignedInUserProfileViewModel

internal class SignedInUserProfileViewModelFactory(
        private val observeSignedInUserInteractor: ObserveSignedInUserInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = SignedInUserProfileViewModel(
            observeSignedInUserInteractor
    ) as T
}
