package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import arrow.core.orNull
import inc.ahmedmourad.sherlock.domain.interactors.auth.SendPasswordResetEmailInteractor
import inc.ahmedmourad.sherlock.model.validators.auth.validateEmail
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class ResetPasswordViewModel(
        private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
) : ViewModel() {

    val email by lazy { MutableLiveData<String?>() }

    val emailError by lazy { MutableLiveData<String?>() }

    fun onCompleteSignUp(): Single<Either<Throwable, Unit>>? {
        return validateEmail(email.value).bimap(
                leftOperation = emailError::setValue,
                rightOperation = {
                    sendPasswordResetEmailInteractor(it)
                            .observeOn(AndroidSchedulers.mainThread())
                }
        ).orNull()
    }

    class Factory(
            private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ResetPasswordViewModel(
                    sendPasswordResetEmailInteractor
            ) as T
        }
    }
}
