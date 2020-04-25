package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.orNull
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateEmail
import dev.ahmedmourad.sherlock.domain.interactors.auth.SendPasswordResetEmailInteractor
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class ResetPasswordViewModel(
        private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor,
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
                    sendPasswordResetEmailInteractor(it)
                            .observeOn(AndroidSchedulers.mainThread())
                }
        ).orNull()
    }

    class Factory(
            private val sendPasswordResetEmailInteractor: SendPasswordResetEmailInteractor,
            owner: SavedStateRegistryOwner,
            email: String?
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs(email)) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return ResetPasswordViewModel(
                    sendPasswordResetEmailInteractor,
                    handle
            ) as T
        }
    }

    companion object {

        private const val KEY_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.EMAIL"
        private const val KEY_ERROR_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.EMAIL"

        private fun defaultArgs(email: String?): Bundle? {
            return email?.let { e ->
                Bundle(1).apply {
                    putString(KEY_EMAIL, e)
                }
            }
        }
    }
}

internal typealias ResetPasswordViewModelFactoryFactory =
        (@JvmSuppressWildcards SavedStateRegistryOwner, @JvmSuppressWildcards String?) ->
        @JvmSuppressWildcards AbstractSavedStateViewModelFactory
