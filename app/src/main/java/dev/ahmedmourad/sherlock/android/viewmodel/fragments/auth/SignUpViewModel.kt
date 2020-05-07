package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import arrow.core.right
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.auth.AppSignUpUser
import dev.ahmedmourad.sherlock.android.model.validators.auth.*
import dev.ahmedmourad.sherlock.android.model.validators.common.validatePicturePath
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignUpInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class SignUpViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val signUpInteractor: Lazy<SignUpInteractor>,
        private val signUpWithGoogleInteractor: Lazy<SignInWithGoogleInteractor>,
        private val signUpWithFacebookInteractor: Lazy<SignInWithFacebookInteractor>,
        private val signUpWithTwitterInteractor: Lazy<SignInWithTwitterInteractor>,
        private val imageLoader: Lazy<ImageLoader>
) : ViewModel() {

    val password: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PASSWORD, null) }
    val passwordConfirmation: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PASSWORD_CONFIRMATION, null) }
    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_EMAIL, null) }
    val displayName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_DISPLAY_NAME, null) }
    val phoneNumberCountryCode: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PHONE_NUMBER_COUNTRY_CODE, null) }
    val phoneNumber: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PHONE_NUMBER, null) }
    val picturePath: LiveData<ImagePicker.PicturePath?>
            by lazy { savedStateHandle.getLiveData<ImagePicker.PicturePath?>(KEY_PICTURE_PATH, null) }

    val passwordError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PASSWORD, null) }
    val passwordConfirmationError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PASSWORD_CONFIRMATION, null) }
    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_EMAIL, null) }
    val credentialsError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_CREDENTIALS, null) }
    val displayNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_DISPLAY_NAME, null) }
    val phoneNumberError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PHONE_NUMBER, null) }
    val picturePathError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PICTURE_PATH, null) }
    val userError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_USER, null) }

    fun onPasswordChange(newValue: String?) {
        savedStateHandle.set(KEY_PASSWORD, newValue)
    }

    fun onPasswordConfirmationChange(newValue: String?) {
        savedStateHandle.set(KEY_PASSWORD_CONFIRMATION, newValue)
    }

    fun onEmailChange(newValue: String?) {
        savedStateHandle.set(KEY_EMAIL, newValue)
    }

    fun onDisplayNameChange(newValue: String?) {
        savedStateHandle.set(KEY_DISPLAY_NAME, newValue)
    }

    fun onPhoneNumberCountryCodeChange(newValue: String?) {
        savedStateHandle.set(KEY_PHONE_NUMBER_COUNTRY_CODE, newValue)
    }

    fun onPhoneNumberChange(newValue: String?) {
        savedStateHandle.set(KEY_PHONE_NUMBER, newValue)
    }

    fun onPicturePathChange(newValue: ImagePicker.PicturePath?) {
        savedStateHandle.set(KEY_PICTURE_PATH, newValue)
    }

    fun onPasswordErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PASSWORD, null)
    }

    fun onPasswordConfirmationErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PASSWORD_CONFIRMATION, null)
    }

    fun onEmailErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onCredentialsErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_CREDENTIALS, null)
    }

    fun onDisplayNameErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_DISPLAY_NAME, null)
    }

    fun onPhoneNumberErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, null)
    }

    fun onPicturePathErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PICTURE_PATH, null)
    }

    fun onUserErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_USER, null)
    }

    fun onSignUpWithGoogle(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return signUpWithGoogleInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onSignUpWithFacebook(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return signUpWithFacebookInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onSignUpWithTwitter(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>> {
        return signUpWithTwitterInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onSignUp(): Single<Either<Throwable, SignedInUser>>? {
        return toAppSignUpUser()?.toSignUpUser(imageLoader.get())?.let {
            signUpInteractor.get().invoke(it).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun toAppSignUpUser(): AppSignUpUser? {
        return Either.fx<Unit, AppSignUpUser> {

            val (email) = validateEmail(email.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_EMAIL, it)
            }

            val (password) = validatePassword(password.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_PASSWORD, it)
            }

            validatePasswordConfirmation(passwordConfirmation.value, password).mapLeft {
                savedStateHandle.set(KEY_ERROR_PASSWORD_CONFIRMATION, it)
            }.bind()

            val (credentials) = validateUserCredentials(email, password).mapLeft {
                savedStateHandle.set(KEY_ERROR_CREDENTIALS, it)
            }

            val (displayName) = validateDisplayName(displayName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_DISPLAY_NAME, it)
            }

            val (phoneNumber) = validatePhoneNumber(
                    phoneNumberCountryCode.value,
                    phoneNumber.value
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, it)
            }

            val (picturePath) = picturePath.value?.let { pp ->
                validatePicturePath(pp.value).mapLeft {
                    savedStateHandle.set(KEY_ERROR_PICTURE_PATH, it)
                }
            } ?: null.right()

            validateAppSignUpUser(
                    credentials,
                    displayName,
                    phoneNumber,
                    picturePath,
                    imageLoader.get()
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_USER, it)
            }.bind()

        }.orNull()
    }

    @Reusable
    class Factory @Inject constructor(
            private val signUpInteractor: Provider<Lazy<SignUpInteractor>>,
            private val signUpWithGoogleInteractor: Provider<Lazy<SignInWithGoogleInteractor>>,
            private val signUpWithFacebookInteractor: Provider<Lazy<SignInWithFacebookInteractor>>,
            private val signUpWithTwitterInteractor: Provider<Lazy<SignInWithTwitterInteractor>>,
            private val imageLoader: Provider<Lazy<ImageLoader>>
    ) : AssistedViewModelFactory<SignUpViewModel> {
        override fun invoke(handle: SavedStateHandle): SignUpViewModel {
            return SignUpViewModel(
                    handle,
                    signUpInteractor.get(),
                    signUpWithGoogleInteractor.get(),
                    signUpWithFacebookInteractor.get(),
                    signUpWithTwitterInteractor.get(),
                    imageLoader.get()
            )
        }
    }

    companion object {

        private const val KEY_PASSWORD =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PASSWORD"
        private const val KEY_PASSWORD_CONFIRMATION =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PASSWORD_CONFIRMATION"
        private const val KEY_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.EMAIL"
        private const val KEY_DISPLAY_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.DISPLAY_NAME"
        private const val KEY_PHONE_NUMBER_COUNTRY_CODE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PHONE_NUMBER_COUNTRY_CODE"
        private const val KEY_PHONE_NUMBER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PHONE_NUMBER"
        private const val KEY_PICTURE_PATH =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PICTURE_PATH"

        private const val KEY_ERROR_PASSWORD =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_PASSWORD"
        private const val KEY_ERROR_PASSWORD_CONFIRMATION =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_PASSWORD_CONFIRMATION"
        private const val KEY_ERROR_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_EMAIL"
        private const val KEY_ERROR_CREDENTIALS =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_CREDENTIALS"
        private const val KEY_ERROR_DISPLAY_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_DISPLAY_NAME"
        private const val KEY_ERROR_PHONE_NUMBER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_PHONE_NUMBER"
        private const val KEY_ERROR_PICTURE_PATH =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_PICTURE_PATH"
        private const val KEY_ERROR_USER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_USER"

        fun defaultArgs(): Bundle? = null
    }
}
