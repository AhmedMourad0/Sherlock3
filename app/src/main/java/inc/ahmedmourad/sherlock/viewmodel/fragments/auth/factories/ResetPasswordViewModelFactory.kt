package inc.ahmedmourad.sherlock.viewmodel.fragments.auth.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.SendPasswordResetEmailInteractor
import inc.ahmedmourad.sherlock.viewmodel.fragments.auth.ResetPasswordViewModel

internal class ResetPasswordViewModelFactory(
        private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ResetPasswordViewModel(
                sendPasswordResetEmailInteractor
        ) as T
    }
}
