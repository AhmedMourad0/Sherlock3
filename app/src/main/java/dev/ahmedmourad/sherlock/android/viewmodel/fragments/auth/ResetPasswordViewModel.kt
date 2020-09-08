package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateEmail
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SendPasswordResetEmailInteractor
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class ResetPasswordViewModel(
        private val sendPasswordResetEmailInteractor: Lazy<SendPasswordResetEmailInteractor>,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var disposable by disposable()

    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_EMAIL, null) }

    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_EMAIL, null) }

    val passwordResetState: LiveData<PasswordResetState?> by lazy {
        Transformations.map(savedStateHandle.getLiveData<Bundle?>(
                KEY_PASSWORD_RESET_STATE,
                null
        )) {
            it?.unbundle(PasswordResetState.serializer())
        }
    }

    fun onEmailChange(newValue: String) {
        savedStateHandle.set(KEY_EMAIL, newValue)
    }

    fun onEmailErrorHandled() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onResetPasswordStateHandled() {
        savedStateHandle.set(KEY_PASSWORD_RESET_STATE, null)
    }

    fun onSendResetEmail() {
        validateEmail(email.value).fold(ifLeft = {
            savedStateHandle.set(KEY_ERROR_EMAIL, it)
        }, ifRight = { email ->
            disposable = sendPasswordResetEmailInteractor.get()
                    .invoke(email)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ either ->
                        either.fold(ifLeft = { e ->
                            when (e) {

                                is SendPasswordResetEmailInteractor.Exception.NonExistentEmailException -> {
                                    savedStateHandle.set(
                                            KEY_PASSWORD_RESET_STATE,
                                            PasswordResetState.NonExistentEmail.bundle(PasswordResetState.serializer())
                                    )
                                }

                                SendPasswordResetEmailInteractor.Exception.NoInternetConnectionException -> {
                                    savedStateHandle.set(
                                            KEY_PASSWORD_RESET_STATE,
                                            PasswordResetState.NoInternet.bundle(PasswordResetState.serializer())
                                    )
                                }

                                is SendPasswordResetEmailInteractor.Exception.UnknownException -> {
                                    Timber.error(e.origin, e.origin::toString)
                                    savedStateHandle.set(
                                            KEY_PASSWORD_RESET_STATE,
                                            PasswordResetState.Error.bundle(PasswordResetState.serializer())
                                    )
                                }
                            }.exhaust()
                        }, ifRight = {
                            savedStateHandle.set(
                                    KEY_PASSWORD_RESET_STATE,
                                    PasswordResetState.Success.bundle(PasswordResetState.serializer())
                            )
                        })
                    }, {
                        savedStateHandle.set(
                                KEY_PASSWORD_RESET_STATE,
                                PasswordResetState.Error.bundle(PasswordResetState.serializer())
                        )
                        Timber.error(it, it::toString)
                    })
        })
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

    @Serializable
    sealed class PasswordResetState {

        @Serializable
        object Success : PasswordResetState()

        @Serializable
        object NonExistentEmail : PasswordResetState()

        @Serializable
        object NoInternet : PasswordResetState()

        @Serializable
        object Error : PasswordResetState()
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
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_EMAIL"
        private const val KEY_PASSWORD_RESET_STATE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PASSWORD_RESET_STATE"

        fun defaultArgs(email: String?): Bundle? {
            return email?.let { e ->
                Bundle(1).apply {
                    putString(KEY_EMAIL, e)
                }
            }
        }
    }
}
