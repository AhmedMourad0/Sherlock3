package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
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
        private val signUpInteractor: SignUpInteractor,
        private val signUpWithGoogleInteractor: SignInWithGoogleInteractor,
        private val signUpWithFacebookInteractor: SignInWithFacebookInteractor,
        private val signUpWithTwitterInteractor: SignInWithTwitterInteractor
) : ViewModel() {

    val password by lazy { MutableLiveData<String?>() }
    val passwordConfirmation by lazy { MutableLiveData<String?>() }
    val email by lazy { MutableLiveData<String?>() }
    val displayName by lazy { MutableLiveData<String?>() }
    val phoneNumberCountryCode by lazy { MutableLiveData<String?>() }
    val phoneNumber by lazy { MutableLiveData<String?>() }
    val picturePath by lazy { MutableLiveData<ImagePicker.PicturePath?>() }

    val passwordError by lazy { MutableLiveData<String?>() }
    val passwordConfirmationError by lazy { MutableLiveData<String?>() }
    val emailError by lazy { MutableLiveData<String?>() }
    val credentialsError by lazy { MutableLiveData<String?>() }
    val displayNameError by lazy { MutableLiveData<String?>() }
    val phoneNumberError by lazy { MutableLiveData<String?>() }
    val picturePathError by lazy { MutableLiveData<String?>() }
    val userError by lazy { MutableLiveData<String?>() }

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

            val (email) = validateEmail(email.value).mapLeft(emailError::setValue)

            val (password) = validatePassword(password.value).mapLeft(passwordError::setValue)

            if (password.value != passwordConfirmation.value) {
                passwordConfirmationError.value = ""
                Unit.left().bind<AppSignUpUser>()
            }

            val (credentials) = validateUserCredentials(email, password).mapLeft(credentialsError::setValue)

            val (displayName) = validateDisplayName(displayName.value).mapLeft(displayNameError::setValue)

            val (phoneNumber) = validatePhoneNumber(
                    phoneNumberCountryCode.value,
                    phoneNumber.value
            ).mapLeft(phoneNumberError::setValue)

            val (picturePath) = picturePath.value?.let {
                validatePicturePath(it.value).mapLeft(picturePathError::setValue)
            } ?: null.right()

            validateAppSignUpUser(
                    credentials,
                    displayName,
                    phoneNumber,
                    picturePath
            ).mapLeft(userError::setValue).bind()

        }.orNull()
    }

    class Factory(
            private val signUpInteractor: SignUpInteractor,
            private val signUpWithGoogleInteractor: SignInWithGoogleInteractor,
            private val signUpWithFacebookInteractor: SignInWithFacebookInteractor,
            private val signUpWithTwitterInteractor: SignInWithTwitterInteractor
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel(
                    signUpInteractor,
                    signUpWithGoogleInteractor,
                    signUpWithFacebookInteractor,
                    signUpWithTwitterInteractor
            ) as T
        }
    }
}
