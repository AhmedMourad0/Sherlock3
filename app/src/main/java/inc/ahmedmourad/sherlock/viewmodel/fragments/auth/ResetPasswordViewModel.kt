package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.orNull
import inc.ahmedmourad.sherlock.domain.interactors.auth.SendPasswordResetEmailInteractor
import inc.ahmedmourad.sherlock.model.validators.auth.validateEmail
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class ResetPasswordViewModel(
        private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_EMAIL, null) }

    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_EMAIL, null) }

    fun onEmailChange(newValue: String) {
        savedStateHandle.set(KEY_EMAIL, newValue)
    }

    fun onEmailErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onCompleteSignUp(): Single<Either<Throwable, Unit>>? {
        return validateEmail(email.value).bimap(
                leftOperation = {
                    savedStateHandle.set(KEY_ERROR_EMAIL, it)
                }, rightOperation = {
                    sendPasswordResetEmailInteractor(it)
                            .observeOn(AndroidSchedulers.mainThread())
                }
        ).orNull()
    }

    class Factory(
            private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor,
            owner: SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return ResetPasswordViewModel(
                    sendPasswordResetEmailInteractor,
                    handle
            ) as T
        }
    }

    companion object {
        const val KEY_EMAIL =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.EMAIL"
        const val KEY_ERROR_EMAIL =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.EMAIL"
    }
}

