package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateEmail
import dev.ahmedmourad.sherlock.android.model.validators.auth.validatePassword
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateUserCredentials
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class SignInViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val signInInteractor: Lazy<SignInInteractor>,
        private val signInWithGoogleInteractor: Lazy<SignInWithGoogleInteractor>,
        private val signInWithFacebookInteractor: Lazy<SignInWithFacebookInteractor>,
        private val signInWithTwitterInteractor: Lazy<SignInWithTwitterInteractor>
) : ViewModel() {

    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_EMAIL, null) }
    val password: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PASSWORD, null) }

    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_EMAIL, null) }
    val passwordError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PASSWORD, null) }
    val credentialsError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_CREDENTIALS, null) }

    fun onEmailChange(newValue: String?) {
        savedStateHandle.set(KEY_EMAIL, newValue)
    }

    fun onPasswordChange(newValue: String?) {
        savedStateHandle.set(KEY_PASSWORD, newValue)
    }

    fun onEmailErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onPasswordErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PASSWORD, null)
    }

    fun onCredentialsErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_CREDENTIALS, null)
    }

    fun onSignInWithGoogle():
            Single<Either<SignInWithGoogleInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
        return signInWithGoogleInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onSignInWithFacebook():
            Single<Either<SignInWithFacebookInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
        return signInWithFacebookInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onSignInWithTwitter():
            Single<Either<SignInWithTwitterInteractor.Exception, Either<IncompleteUser, SignedInUser>>> {
        return signInWithTwitterInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onSignIn():
            Single<Either<SignInInteractor.Exception, Either<IncompleteUser, SignedInUser>>>? {
        return toUserCredentials()?.let {
            signInInteractor.get().invoke(it).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun toUserCredentials(): UserCredentials? {
        return Either.fx<Unit, UserCredentials> {

            val (email) = validateEmail(email.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_EMAIL, it)
            }

            val (password) = validatePassword(password.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_PASSWORD, it)
            }

            validateUserCredentials(
                    email,
                    password
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_CREDENTIALS, it)
            }.bind()

        }.orNull()
    }

    @Reusable
    class Factory @Inject constructor(
            private val signInInteractor: Provider<Lazy<SignInInteractor>>,
            private val signInWithGoogleInteractor: Provider<Lazy<SignInWithGoogleInteractor>>,
            private val signInWithFacebookInteractor: Provider<Lazy<SignInWithFacebookInteractor>>,
            private val signInWithTwitterInteractor: Provider<Lazy<SignInWithTwitterInteractor>>
    ) : AssistedViewModelFactory<SignInViewModel> {
        override fun invoke(handle: SavedStateHandle): SignInViewModel {
            return SignInViewModel(
                    handle,
                    signInInteractor.get(),
                    signInWithGoogleInteractor.get(),
                    signInWithFacebookInteractor.get(),
                    signInWithTwitterInteractor.get()
            )
        }
    }

    companion object {

        private const val KEY_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.EMAIL"
        private const val KEY_PASSWORD =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PASSWORD"

        private const val KEY_ERROR_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_EMAIL"
        private const val KEY_ERROR_PASSWORD =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_PASSWORD"
        private const val KEY_ERROR_CREDENTIALS =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_CREDENTIALS"

        fun defaultArgs(): Bundle? = null
    }
}
