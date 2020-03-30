package inc.ahmedmourad.sherlock.viewmodel.controllers.auth.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.domain.interactors.auth.CompleteSignUpInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.viewmodel.controllers.auth.CompleteSignUpViewModel

internal typealias CompleteSignUpViewModelFactoryFactory =
        (@JvmSuppressWildcards IncompleteUser) -> @JvmSuppressWildcards ViewModelProvider.NewInstanceFactory

internal fun completeSignUpViewModelFactoryFactory(
        completeSignUpInteractor: CompleteSignUpInteractor,
        incompleteUser: IncompleteUser
): CompleteSignUpViewModelFactory {
    return CompleteSignUpViewModelFactory(incompleteUser, completeSignUpInteractor)
}


internal class CompleteSignUpViewModelFactory(
        private val incompleteUser: IncompleteUser,
        private val completeSignUpInteractor: CompleteSignUpInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = CompleteSignUpViewModel(
            incompleteUser,
            completeSignUpInteractor
    ) as T
}
