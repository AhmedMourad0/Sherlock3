package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.auth.AppSignUpUser
import dev.ahmedmourad.sherlock.android.model.validators.auth.*
import dev.ahmedmourad.sherlock.android.model.validators.common.validatePicturePathNullable
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignUpInteractor
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error
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

    private var disposable by disposable()

    val password: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PASSWORD, null) }
    val passwordConfirmation: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PASSWORD_CONFIRMATION, null) }
    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_EMAIL, null) }
    val displayName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_DISPLAY_NAME, null) }
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

    val signUpState: LiveData<SignUpState?> by lazy {
        Transformations.map(savedStateHandle.getLiveData<Bundle?>(
                KEY_SIGN_UP_STATE,
                null
        )) {
            it?.unbundle(SignUpState.serializer())
        }
    }

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

    fun onPhoneNumberChange(newValue: String?) {
        savedStateHandle.set(KEY_PHONE_NUMBER, newValue)
    }

    fun onPicturePathChange(newValue: ImagePicker.PicturePath?) {
        savedStateHandle.set(KEY_PICTURE_PATH, newValue)
    }

    fun onPasswordErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PASSWORD, null)
    }

    fun onPasswordConfirmationErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PASSWORD_CONFIRMATION, null)
    }

    fun onEmailErrorHandled() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onCredentialsErrorHandled() {
        savedStateHandle.set(KEY_ERROR_CREDENTIALS, null)
    }

    fun onDisplayNameErrorHandled() {
        savedStateHandle.set(KEY_ERROR_DISPLAY_NAME, null)
    }

    fun onPhoneNumberErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, null)
    }

    fun onPicturePathErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PICTURE_PATH, null)
    }

    fun onUserErrorHandled() {
        savedStateHandle.set(KEY_ERROR_USER, null)
    }

    fun onSignUpStateHandled() {
        savedStateHandle.set(KEY_SIGN_UP_STATE, null)
    }

    fun onSignUpWithGoogle() {
        disposable = signUpWithGoogleInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->
                    either.fold(ifLeft = { e ->
                        when (e) {

                            SignInWithGoogleInteractor.Exception.AccountHasBeenDisabledException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.AccountDisabled.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.MalformedOrExpiredCredentialException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.MalformedOrExpiredCredential.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.EmailAlreadyInUseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.EmailAlreadyInUse.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.NoResponseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.NoResponse.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.NoInternetConnectionException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.NoInternet.bundle(SignUpState.serializer())
                                )
                            }

                            is SignInWithGoogleInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.Error.bundle(SignUpState.serializer())
                                )
                            }

                            is SignInWithGoogleInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.Error.bundle(SignUpState.serializer())
                                )
                            }
                        }.exhaust()
                    }, ifRight = {
                        savedStateHandle.set(
                                KEY_SIGN_UP_STATE,
                                SignUpState.Success.bundle(SignUpState.serializer())
                        )
                    })
                }, {
                    Timber.error(it, it::toString)
                    savedStateHandle.set(
                            KEY_SIGN_UP_STATE,
                            SignUpState.Error.bundle(SignUpState.serializer())
                    )
                })
    }

    fun onSignUpWithFacebook() {
        disposable = signUpWithFacebookInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->
                    either.fold(ifLeft = { e ->
                        when (e) {

                            SignInWithFacebookInteractor.Exception.AccountHasBeenDisabledException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.AccountDisabled.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.MalformedOrExpiredCredentialException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.MalformedOrExpiredCredential.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.EmailAlreadyInUseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.EmailAlreadyInUse.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.NoResponseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.NoResponse.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.NoInternetConnectionException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.NoInternet.bundle(SignUpState.serializer())
                                )
                            }

                            is SignInWithFacebookInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.Error.bundle(SignUpState.serializer())
                                )
                            }

                            is SignInWithFacebookInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.Error.bundle(SignUpState.serializer())
                                )
                            }
                        }.exhaust()
                    }, ifRight = {
                        savedStateHandle.set(
                                KEY_SIGN_UP_STATE,
                                SignUpState.Success.bundle(SignUpState.serializer())
                        )
                    })
                }, {
                    Timber.error(it, it::toString)
                    savedStateHandle.set(
                            KEY_SIGN_UP_STATE,
                            SignUpState.Error.bundle(SignUpState.serializer())
                    )
                })
    }

    fun onSignUpWithTwitter() {
        disposable = signUpWithTwitterInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->
                    either.fold(ifLeft = { e ->
                        when (e) {

                            SignInWithTwitterInteractor.Exception.AccountHasBeenDisabledException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.AccountDisabled.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.MalformedOrExpiredCredentialException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.MalformedOrExpiredCredential.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.EmailAlreadyInUseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.EmailAlreadyInUse.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.NoResponseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.NoResponse.bundle(SignUpState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.NoInternetConnectionException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.NoInternet.bundle(SignUpState.serializer())
                                )
                            }

                            is SignInWithTwitterInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.Error.bundle(SignUpState.serializer())
                                )
                            }

                            is SignInWithTwitterInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_UP_STATE,
                                        SignUpState.Error.bundle(SignUpState.serializer())
                                )
                            }
                        }.exhaust()
                    }, ifRight = {
                        savedStateHandle.set(
                                KEY_SIGN_UP_STATE,
                                SignUpState.Success.bundle(SignUpState.serializer())
                        )
                    })
                }, {
                    Timber.error(it, it::toString)
                    savedStateHandle.set(
                            KEY_SIGN_UP_STATE,
                            SignUpState.Error.bundle(SignUpState.serializer())
                    )
                })
    }

    fun onSignUp() {
        disposable = toAppSignUpUser()?.toSignUpUser(imageLoader.get())?.let { user ->
            signUpInteractor.get()
                    .invoke(user)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ either ->
                        either.fold(ifLeft = { e ->
                            when (e) {

                                SignUpInteractor.Exception.WeakPasswordException -> {
                                    savedStateHandle.set(
                                            KEY_SIGN_UP_STATE,
                                            SignUpState.WeakPassword.bundle(SignUpState.serializer())
                                    )
                                }

                                SignUpInteractor.Exception.MalformedEmailException -> {
                                    savedStateHandle.set(
                                            KEY_SIGN_UP_STATE,
                                            SignUpState.MalformedEmail.bundle(SignUpState.serializer())
                                    )
                                }

                                is SignUpInteractor.Exception.EmailAlreadyInUseException -> {
                                    savedStateHandle.set(
                                            KEY_SIGN_UP_STATE,
                                            SignUpState.EmailAlreadyInUse.bundle(SignUpState.serializer())
                                    )
                                }

                                SignUpInteractor.Exception.NoInternetConnectionException -> {
                                    savedStateHandle.set(
                                            KEY_SIGN_UP_STATE,
                                            SignUpState.NoInternet.bundle(SignUpState.serializer())
                                    )
                                }

                                is SignUpInteractor.Exception.InternalException -> {
                                    Timber.error(e.origin, e.origin::toString)
                                    savedStateHandle.set(
                                            KEY_SIGN_UP_STATE,
                                            SignUpState.Error.bundle(SignUpState.serializer())
                                    )
                                }

                                is SignUpInteractor.Exception.UnknownException -> {
                                    Timber.error(e.origin, e.origin::toString)
                                    savedStateHandle.set(
                                            KEY_SIGN_UP_STATE,
                                            SignUpState.Error.bundle(SignUpState.serializer())
                                    )
                                }
                            }.exhaust()
                        }, ifRight = {
                            savedStateHandle.set(
                                    KEY_SIGN_UP_STATE,
                                    SignUpState.Success.bundle(SignUpState.serializer())
                            )
                        })
                    }, {
                        Timber.error(it, it::toString)
                        savedStateHandle.set(
                                KEY_SIGN_UP_STATE,
                                SignUpState.Error.bundle(SignUpState.serializer())
                        )
                    })
        }
    }

    private fun toAppSignUpUser(): AppSignUpUser? {
        return Either.fx<Unit, AppSignUpUser> {

            val email = !validateEmail(email.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_EMAIL, it)
            }

            val password = !validatePassword(password.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_PASSWORD, it)
            }

            validatePasswordConfirmation(passwordConfirmation.value, password).mapLeft {
                savedStateHandle.set(KEY_ERROR_PASSWORD_CONFIRMATION, it)
            }.bind()

            val credentials = !validateUserCredentials(email, password).mapLeft {
                savedStateHandle.set(KEY_ERROR_CREDENTIALS, it)
            }

            val displayName = !validateDisplayName(displayName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_DISPLAY_NAME, it)
            }

            val phoneNumber = !validatePhoneNumber(
                    phoneNumber.value
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, it)
            }

            val picturePath = !validatePicturePathNullable(picturePath.value?.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_PICTURE_PATH, it)
            }

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

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

    @Serializable
    sealed class SignUpState {

        @Serializable
        object Success : SignUpState()

        @Serializable
        object AccountDisabled : SignUpState()

        @Serializable
        object MalformedOrExpiredCredential : SignUpState()

        @Serializable
        object EmailAlreadyInUse : SignUpState()

        @Serializable
        object NoResponse : SignUpState()

        @Serializable
        object NoInternet : SignUpState()

        @Serializable
        object WeakPassword : SignUpState()

        @Serializable
        object MalformedEmail : SignUpState()

        @Serializable
        object Error : SignUpState()
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

        private const val KEY_SIGN_UP_STATE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.SIGN_UP_STATE"

        fun defaultArgs(): Bundle? = null
    }
}
