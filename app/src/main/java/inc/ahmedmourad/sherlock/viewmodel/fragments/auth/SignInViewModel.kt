package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
        private val signInInteractor: SignInInteractor,
        private val signInWithGoogleInteractor: SignInWithGoogleInteractor,
        private val signInWithFacebookInteractor: SignInWithFacebookInteractor,
        private val signInWithTwitterInteractor: SignInWithTwitterInteractor
) : ViewModel() {

    val email by lazy { MutableLiveData<String?>("") }
    val password by lazy { MutableLiveData<String?>("") }

    val emailError by lazy { MutableLiveData<String?>() }
    val passwordError by lazy { MutableLiveData<String?>() }
    val credentailsError by lazy { MutableLiveData<String?>() }

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

            val (email) = validateEmail(email.value).mapLeft(emailError::setValue)

            val (password) = validatePassword(password.value).mapLeft(passwordError::setValue)

            validateUserCredentials(
                    email,
                    password
            ).mapLeft(credentailsError::setValue).bind()

        }.orNull()
    }
}
