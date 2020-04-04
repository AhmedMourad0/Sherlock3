package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.orNull
import inc.ahmedmourad.sherlock.domain.interactors.auth.SendPasswordResetEmailInteractor
import inc.ahmedmourad.sherlock.validators.auth.validateEmail
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class ResetPasswordViewModel(
        private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor
) : ViewModel() {

    val email by lazy { MutableLiveData<String?>("") }

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
}
