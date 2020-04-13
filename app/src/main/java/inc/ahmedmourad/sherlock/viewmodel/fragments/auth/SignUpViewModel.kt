package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignUpInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.model.auth.AppSignUpUser
import inc.ahmedmourad.sherlock.model.validators.auth.*
import inc.ahmedmourad.sherlock.model.validators.common.validatePicturePath
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SignUpViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val signUpInteractor: SignUpInteractor,
        private val signUpWithGoogleInteractor: SignInWithGoogleInteractor,
        private val signUpWithFacebookInteractor: SignInWithFacebookInteractor,
        private val signUpWithTwitterInteractor: SignInWithTwitterInteractor
) : ViewModel() {

    val password: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_PASSWORD, null) }
    val passwordConfirmation: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_PASSWORD_CONFIRMATION, null) }
    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_EMAIL, null) }
    val displayName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_DISPLAY_NAME, null) }
    val phoneNumberCountryCode: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_PHONE_NUMBER_COUNTRY_CODE, null) }
    val phoneNumber: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_PHONE_NUMBER, null) }
    val picturePath: LiveData<ImagePicker.PicturePath?>
            by lazy { savedStateHandle.getLiveData(KEY_PICTURE_PATH, null) }

    val passwordError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PASSWORD, null) }
    val passwordConfirmationError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PASSWORD_CONFIRMATION, null) }
    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_EMAIL, null) }
    val credentialsError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_CREDENTIALS, null) }
    val displayNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_DISPLAY_NAME, null) }
    val phoneNumberError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PHONE_NUMBER, null) }
    val picturePathError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PICTURE_PATH, null) }
    val userError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_USER, null) }

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

    fun onSignUpWithGoogle() = signUpWithGoogleInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    fun onSignUpWithFacebook() = signUpWithFacebookInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    fun onSignUpWithTwitter() = signUpWithTwitterInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    fun onSignUp(): Single<Either<Throwable, SignedInUser>>? {
        return toAppSignUpUser()?.toSignUpUser()?.let {
            signUpInteractor(it).observeOn(AndroidSchedulers.mainThread())
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
                    picturePath
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_USER, it)
            }.bind()

        }.orNull()
    }

    class Factory(
            owner: SavedStateRegistryOwner,
            private val signUpInteractor: SignUpInteractor,
            private val signUpWithGoogleInteractor: SignInWithGoogleInteractor,
            private val signUpWithFacebookInteractor: SignInWithFacebookInteractor,
            private val signUpWithTwitterInteractor: SignInWithTwitterInteractor
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return SignUpViewModel(
                    handle,
                    signUpInteractor,
                    signUpWithGoogleInteractor,
                    signUpWithFacebookInteractor,
                    signUpWithTwitterInteractor
            ) as T
        }
    }

    companion object {

        private const val KEY_PASSWORD =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.PASSWORD"
        private const val KEY_PASSWORD_CONFIRMATION =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.PASSWORD_CONFIRMATION"
        private const val KEY_EMAIL =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.EMAIL"
        private const val KEY_DISPLAY_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.DISPLAY_NAME"
        private const val KEY_PHONE_NUMBER_COUNTRY_CODE =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.PHONE_NUMBER_COUNTRY_CODE"
        private const val KEY_PHONE_NUMBER =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.PHONE_NUMBER"
        private const val KEY_PICTURE_PATH =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.PICTURE_PATH"

        private const val KEY_ERROR_PASSWORD =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_PASSWORD"
        private const val KEY_ERROR_PASSWORD_CONFIRMATION =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_PASSWORD_CONFIRMATION"
        private const val KEY_ERROR_EMAIL =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_EMAIL"
        private const val KEY_ERROR_CREDENTIALS =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_CREDENTIALS"
        private const val KEY_ERROR_DISPLAY_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_DISPLAY_NAME"
        private const val KEY_ERROR_PHONE_NUMBER =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_PHONE_NUMBER"
        private const val KEY_ERROR_PICTURE_PATH =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_PICTURE_PATH"
        private const val KEY_ERROR_USER =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_USER"
    }
}
