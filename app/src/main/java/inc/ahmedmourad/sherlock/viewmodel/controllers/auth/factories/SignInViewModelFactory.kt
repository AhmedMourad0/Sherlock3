package inc.ahmedmourad.sherlock.viewmodel.controllers.auth.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.SignInViewModel

internal class SignInViewModelFactory(
        private val signInInteractor: SignInInteractor,
        private val signInWithGoogleInteractor: SignInWithGoogleInteractor,
        private val signInWithFacebookInteractor: SignInWithFacebookInteractor,
        private val signInWithTwitterInteractor: SignInWithTwitterInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = SignInViewModel(
            signInInteractor,
            signInWithGoogleInteractor,
            signInWithFacebookInteractor,
            signInWithTwitterInteractor
    ) as T
}
