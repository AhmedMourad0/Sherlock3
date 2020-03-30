package inc.ahmedmourad.sherlock.viewmodel.controllers.auth.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.FindSignedInUserInteractor
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.SignedInUserProfileViewModel

internal class SignedInUserProfileViewModelFactory(
        private val findSignedInUserInteractor: FindSignedInUserInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = SignedInUserProfileViewModel(
            findSignedInUserInteractor
    ) as T
}
