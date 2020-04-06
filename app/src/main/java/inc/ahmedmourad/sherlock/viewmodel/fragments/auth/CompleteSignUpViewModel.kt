package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import inc.ahmedmourad.sherlock.domain.interactors.auth.CompleteSignUpInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.common.PicturePath
import inc.ahmedmourad.sherlock.model.auth.AppCompletedUser
import inc.ahmedmourad.sherlock.model.validators.auth.validateAppCompletedUser
import inc.ahmedmourad.sherlock.model.validators.auth.validateDisplayName
import inc.ahmedmourad.sherlock.model.validators.auth.validateEmail
import inc.ahmedmourad.sherlock.model.validators.auth.validatePhoneNumber
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class CompleteSignUpViewModel(
        private val incompleteUser: IncompleteUser,
        private val completeSignUpInteractor: CompleteSignUpInteractor
) : ViewModel() {

    val email by lazy { MutableLiveData<String?>(incompleteUser.email?.value) }
    val displayName by lazy { MutableLiveData<String?>(incompleteUser.displayName?.value) }
    val phoneNumberCountryCode by lazy { MutableLiveData<String?>(incompleteUser.phoneNumber?.countryCode) }
    val phoneNumber by lazy { MutableLiveData<String?>(incompleteUser.phoneNumber?.number) }
    val picturePath by lazy { MutableLiveData<PicturePath?>() }

    val emailError by lazy { MutableLiveData<String?>() }
    val displayNameError by lazy { MutableLiveData<String?>() }
    val phoneNumberError by lazy { MutableLiveData<String?>() }
    val userError by lazy { MutableLiveData<String?>() }

    fun onCompleteSignUp(): Single<Either<Throwable, SignedInUser>>? {
        return toCompletedUser()?.let {
            completeSignUpInteractor(it.toCompletedUser()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun toCompletedUser(): AppCompletedUser? {
        return Either.fx<Unit, AppCompletedUser> {

            val (email) = validateEmail(email.value).mapLeft(emailError::setValue)

            val (displayName) = validateDisplayName(displayName.value).mapLeft(displayNameError::setValue)

            val (phoneNumber) = validatePhoneNumber(
                    phoneNumberCountryCode.value,
                    phoneNumber.value
            ).mapLeft(phoneNumberError::setValue)

            validateAppCompletedUser(
                    incompleteUser.id,
                    email,
                    displayName,
                    phoneNumber,
                    picturePath.value
            ).mapLeft(userError::setValue).bind()

        }.orNull()
    }
}
