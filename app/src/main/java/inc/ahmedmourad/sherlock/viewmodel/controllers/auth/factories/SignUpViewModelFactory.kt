package inc.ahmedmourad.sherlock.viewmodel.controllers.auth.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignUpInteractor
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.SignUpViewModel

internal class SignUpViewModelFactory(
        private val signUpInteractor: SignUpInteractor,
        private val signUpWithGoogleInteractor: SignInWithGoogleInteractor,
        private val signUpWithFacebookInteractor: SignInWithFacebookInteractor,
        private val signUpWithTwitterInteractor: SignInWithTwitterInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = SignUpViewModel(
            signUpInteractor,
            signUpWithGoogleInteractor,
            signUpWithFacebookInteractor,
            signUpWithTwitterInteractor
    ) as T
}
