package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithFacebookInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithGoogleInteractor
import inc.ahmedmourad.sherlock.domain.interactors.auth.SignInWithTwitterInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import inc.ahmedmourad.sherlock.model.validators.auth.validateEmail
import inc.ahmedmourad.sherlock.model.validators.auth.validatePassword
import inc.ahmedmourad.sherlock.model.validators.auth.validateUserCredentials
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SignInViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val signInInteractor: SignInInteractor,
        private val signInWithGoogleInteractor: SignInWithGoogleInteractor,
        private val signInWithFacebookInteractor: SignInWithFacebookInteractor,
        private val signInWithTwitterInteractor: SignInWithTwitterInteractor
) : ViewModel() {

    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_EMAIL, null) }
    val password: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_PASSWORD, null) }

    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_EMAIL, null) }
    val passwordError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PASSWORD, null) }
    val credentialsError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_CREDENTIALS, null) }

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

    fun onSignInWithGoogle() = signInWithGoogleInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    fun onSignInWithFacebook() = signInWithFacebookInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    fun onSignInWithTwitter() = signInWithTwitterInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    fun onSignIn(): Single<Either<Throwable, Either<IncompleteUser, SignedInUser>>>? {
        return toUserCredentials()?.let {
            signInInteractor(it).observeOn(AndroidSchedulers.mainThread())
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

    class Factory(
            owner: SavedStateRegistryOwner,
            private val signInInteractor: SignInInteractor,
            private val signInWithGoogleInteractor: SignInWithGoogleInteractor,
            private val signInWithFacebookInteractor: SignInWithFacebookInteractor,
            private val signInWithTwitterInteractor: SignInWithTwitterInteractor
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return SignInViewModel(
                    handle,
                    signInInteractor,
                    signInWithGoogleInteractor,
                    signInWithFacebookInteractor,
                    signInWithTwitterInteractor
            ) as T
        }
    }

    companion object {

        private const val KEY_EMAIL =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.EMAIL"
        private const val KEY_PASSWORD =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.PASSWORD"

        private const val KEY_ERROR_EMAIL =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_EMAIL"
        private const val KEY_ERROR_PASSWORD =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_PASSWORD"
        private const val KEY_ERROR_CREDENTIALS =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_CREDENTIALS"
    }
}
