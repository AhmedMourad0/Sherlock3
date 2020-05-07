package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.orNull
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateEmail
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SendPasswordResetEmailInteractor
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class ResetPasswordViewModel(
        private val sendPasswordResetEmailInteractor: Lazy<SendPasswordResetEmailInteractor>,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_EMAIL, null) }

    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_EMAIL, null) }

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
            sendPasswordResetEmailInteractor.get()
                    .invoke(it)
                            .observeOn(AndroidSchedulers.mainThread())
                }
        ).orNull()
    }

    @Reusable
    class Factory @Inject constructor(
            private val sendPasswordResetEmailInteractor: Provider<Lazy<SendPasswordResetEmailInteractor>>
    ) : AssistedViewModelFactory<ResetPasswordViewModel> {
        override fun invoke(handle: SavedStateHandle): ResetPasswordViewModel {
            return ResetPasswordViewModel(
                    sendPasswordResetEmailInteractor.get(),
                    handle
            )
        }
    }

    companion object {

        private const val KEY_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.EMAIL"
        private const val KEY_ERROR_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.EMAIL"

        fun defaultArgs(email: String?): Bundle? {
            return email?.let { e ->
                Bundle(1).apply {
                    putString(KEY_EMAIL, e)
                }
            }
        }
    }
}
