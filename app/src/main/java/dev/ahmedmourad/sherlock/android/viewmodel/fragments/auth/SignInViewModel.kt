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
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateEmail
import dev.ahmedmourad.sherlock.android.model.validators.auth.validatePassword
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateUserCredentials
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import dev.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class SignInViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val signInInteractor: Lazy<SignInInteractor>,
        private val signInWithGoogleInteractor: Lazy<SignInWithGoogleInteractor>,
        private val signInWithFacebookInteractor: Lazy<SignInWithFacebookInteractor>,
        private val signInWithTwitterInteractor: Lazy<SignInWithTwitterInteractor>
) : ViewModel() {

    private var disposable by disposable()

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

    val signInState: LiveData<SignInState?> by lazy {
        Transformations.map(savedStateHandle.getLiveData<Bundle?>(
                KEY_SIGN_IN_STATE,
                null
        )) {
            it?.unbundle(SignInState.serializer())
        }
    }

    fun onEmailChange(newValue: String?) {
        savedStateHandle.set(KEY_EMAIL, newValue)
    }

    fun onPasswordChange(newValue: String?) {
        savedStateHandle.set(KEY_PASSWORD, newValue)
    }

    fun onEmailErrorHandled() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onPasswordErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PASSWORD, null)
    }

    fun onCredentialsErrorHandled() {
        savedStateHandle.set(KEY_ERROR_CREDENTIALS, null)
    }

    fun onSignInStateHandled() {
        savedStateHandle.set(KEY_SIGN_IN_STATE, null)
    }

    fun onSignInWithGoogle() {
        disposable = signInWithGoogleInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->
                    either.fold(ifLeft = { e ->
                        when (e) {

                            SignInWithGoogleInteractor.Exception.AccountHasBeenDisabledException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.AccountDisabled.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.MalformedOrExpiredCredentialException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.MalformedOrExpiredCredential.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.EmailAlreadyInUseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.EmailAlreadyInUse.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.NoResponseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.NoResponse.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithGoogleInteractor.Exception.NoInternetConnectionException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.NoInternet.bundle(SignInState.serializer())
                                )
                            }

                            is SignInWithGoogleInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.Error.bundle(SignInState.serializer())
                                )
                            }

                            is SignInWithGoogleInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.Error.bundle(SignInState.serializer())
                                )
                            }
                        }.exhaust()
                    }, ifRight = {
                        savedStateHandle.set(
                                KEY_SIGN_IN_STATE,
                                SignInState.Success.bundle(SignInState.serializer())
                        )
                    })
                }, {
                    Timber.error(it, it::toString)
                    savedStateHandle.set(
                            KEY_SIGN_IN_STATE,
                            SignInState.Error.bundle(SignInState.serializer())
                    )
                })
    }

    fun onSignInWithFacebook() {
        disposable = signInWithFacebookInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->
                    either.fold(ifLeft = { e ->
                        when (e) {

                            SignInWithFacebookInteractor.Exception.AccountHasBeenDisabledException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.AccountDisabled.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.MalformedOrExpiredCredentialException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.MalformedOrExpiredCredential.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.EmailAlreadyInUseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.EmailAlreadyInUse.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.NoResponseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.NoResponse.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithFacebookInteractor.Exception.NoInternetConnectionException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.NoInternet.bundle(SignInState.serializer())
                                )
                            }

                            is SignInWithFacebookInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.Error.bundle(SignInState.serializer())
                                )
                            }

                            is SignInWithFacebookInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.Error.bundle(SignInState.serializer())
                                )
                            }
                        }.exhaust()
                    }, ifRight = {
                        savedStateHandle.set(
                                KEY_SIGN_IN_STATE,
                                SignInState.Success.bundle(SignInState.serializer())
                        )
                    })
                }, {
                    Timber.error(it, it::toString)
                    savedStateHandle.set(
                            KEY_SIGN_IN_STATE,
                            SignInState.Error.bundle(SignInState.serializer())
                    )
                })
    }

    fun onSignInWithTwitter() {
        disposable = signInWithTwitterInteractor.get()
                .invoke()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ either ->
                    either.fold(ifLeft = { e ->
                        when (e) {

                            SignInWithTwitterInteractor.Exception.AccountHasBeenDisabledException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.AccountDisabled.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.MalformedOrExpiredCredentialException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.MalformedOrExpiredCredential.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.EmailAlreadyInUseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.EmailAlreadyInUse.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.NoResponseException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.NoResponse.bundle(SignInState.serializer())
                                )
                            }

                            SignInWithTwitterInteractor.Exception.NoInternetConnectionException -> {
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.NoInternet.bundle(SignInState.serializer())
                                )
                            }

                            is SignInWithTwitterInteractor.Exception.InternalException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.Error.bundle(SignInState.serializer())
                                )
                            }

                            is SignInWithTwitterInteractor.Exception.UnknownException -> {
                                Timber.error(e.origin, e.origin::toString)
                                savedStateHandle.set(
                                        KEY_SIGN_IN_STATE,
                                        SignInState.Error.bundle(SignInState.serializer())
                                )
                            }
                        }.exhaust()
                    }, ifRight = {
                        savedStateHandle.set(
                                KEY_SIGN_IN_STATE,
                                SignInState.Success.bundle(SignInState.serializer())
                        )
                    })
                }, {
                    Timber.error(it, it::toString)
                    savedStateHandle.set(
                            KEY_SIGN_IN_STATE,
                            SignInState.Error.bundle(SignInState.serializer())
                    )
                })
    }

    fun onSignIn() {
        disposable = toUserCredentials()?.let {
            signInInteractor.get()
                    .invoke(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ either ->
                        either.fold(ifLeft = { e ->
                            when (e) {

                                SignInInteractor.Exception.AccountDoesNotExistOrHasBeenDisabledException -> {
                                    savedStateHandle.set(
                                            KEY_SIGN_IN_STATE,
                                            SignInState.AccountDisabledOrDoesNotExist.bundle(SignInState.serializer())
                                    )
                                }

                                SignInInteractor.Exception.WrongPasswordException -> {
                                    savedStateHandle.set(
                                            KEY_SIGN_IN_STATE,
                                            SignInState.WrongPassword.bundle(SignInState.serializer())
                                    )
                                }

                                SignInInteractor.Exception.NoInternetConnectionException -> {
                                    savedStateHandle.set(
                                            KEY_SIGN_IN_STATE,
                                            SignInState.NoInternet.bundle(SignInState.serializer())
                                    )
                                }

                                is SignInInteractor.Exception.InternalException -> {
                                    Timber.error(e.origin, e.origin::toString)
                                    savedStateHandle.set(
                                            KEY_SIGN_IN_STATE,
                                            SignInState.Error.bundle(SignInState.serializer())
                                    )
                                }

                                is SignInInteractor.Exception.UnknownException -> {
                                    Timber.error(e.origin, e.origin::toString)
                                    savedStateHandle.set(
                                            KEY_SIGN_IN_STATE,
                                            SignInState.Error.bundle(SignInState.serializer())
                                    )
                                }
                            }.exhaust()
                        }, ifRight = {
                            savedStateHandle.set(
                                    KEY_SIGN_IN_STATE,
                                    SignInState.Success.bundle(SignInState.serializer())
                            )
                        })
                    }, {
                        Timber.error(it, it::toString)
                        savedStateHandle.set(
                                KEY_SIGN_IN_STATE,
                                SignInState.Error.bundle(SignInState.serializer())
                        )
                    })
        }
    }

    private fun toUserCredentials(): UserCredentials? {
        return Either.fx<Unit, UserCredentials> {

            val email = !validateEmail(email.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_EMAIL, it)
            }

            val password = !validatePassword(password.value).mapLeft {
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

    @Serializable
    sealed class SignInState {

        @Serializable
        object Success : SignInState()

        @Serializable
        object AccountDisabled : SignInState()

        @Serializable
        object MalformedOrExpiredCredential : SignInState()

        @Serializable
        object EmailAlreadyInUse : SignInState()

        @Serializable
        object NoResponse : SignInState()

        @Serializable
        object NoInternet : SignInState()

        @Serializable
        object AccountDisabledOrDoesNotExist : SignInState()

        @Serializable
        object WrongPassword : SignInState()

        @Serializable
        object Error : SignInState()
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
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

        private const val KEY_SIGN_IN_STATE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.SIGN_IN_STATE"

        fun defaultArgs(): Bundle? = null
    }
}
