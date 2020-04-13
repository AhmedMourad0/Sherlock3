package inc.ahmedmourad.sherlock.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import arrow.core.right
import inc.ahmedmourad.sherlock.bundlizer.bundle
import inc.ahmedmourad.sherlock.bundlizer.unbundle
import inc.ahmedmourad.sherlock.domain.interactors.auth.CompleteSignUpInteractor
import inc.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import inc.ahmedmourad.sherlock.model.auth.AppCompletedUser
import inc.ahmedmourad.sherlock.model.validators.auth.validateAppCompletedUser
import inc.ahmedmourad.sherlock.model.validators.auth.validateDisplayName
import inc.ahmedmourad.sherlock.model.validators.auth.validateEmail
import inc.ahmedmourad.sherlock.model.validators.auth.validatePhoneNumber
import inc.ahmedmourad.sherlock.model.validators.common.validatePicturePath
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal class CompleteSignUpViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val completeSignUpInteractor: CompleteSignUpInteractor
) : ViewModel() {

    private val id: UserId = savedStateHandle.get<Bundle>(KEY_ID)?.unbundle(UserId.serializer())!!

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

    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_EMAIL, null) }
    val displayNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_DISPLAY_NAME, null) }
    val phoneNumberError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PHONE_NUMBER, null) }
    val picturePathError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PICTURE_PATH, null) }
    val userError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_USER, null) }

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

    fun onCompleteSignUp(): Single<Either<Throwable, SignedInUser>>? {
        return toCompletedUser()?.let {
            completeSignUpInteractor(it.toCompletedUser()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun toCompletedUser(): AppCompletedUser? {
        return Either.fx<Unit, AppCompletedUser> {

            val (email) = validateEmail(email.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_EMAIL, it)
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

            validateAppCompletedUser(
                    id,
                    email,
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
            incompleteUser: IncompleteUser,
            private val completeSignUpInteractor: CompleteSignUpInteractor
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs(incompleteUser)) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
        ): T {
            return CompleteSignUpViewModel(
                    handle,
                    completeSignUpInteractor
            ) as T
        }
    }

    companion object {

        private const val KEY_ID =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ID"

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

        private const val KEY_ERROR_EMAIL =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_EMAIL"
        private const val KEY_ERROR_DISPLAY_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_DISPLAY_NAME"
        private const val KEY_ERROR_PHONE_NUMBER =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_PHONE_NUMBER"
        private const val KEY_ERROR_PICTURE_PATH =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_PICTURE_PATH"
        private const val KEY_ERROR_USER =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.auth.key.ERROR_USER"

        fun defaultArgs(incompleteUser: IncompleteUser): Bundle {
            return Bundle(6).apply {
                putBundle(KEY_ID, incompleteUser.id.bundle(UserId.serializer()))
                putString(KEY_EMAIL, incompleteUser.email?.value)
                putString(KEY_DISPLAY_NAME, incompleteUser.displayName?.value)
                putString(KEY_PHONE_NUMBER_COUNTRY_CODE, incompleteUser.phoneNumber?.countryCode)
                putString(KEY_PHONE_NUMBER, incompleteUser.phoneNumber?.number)
                putParcelable(KEY_PICTURE_PATH, incompleteUser.pictureUrl?.value?.let(ImagePicker::PicturePath))
            }
        }
    }
}

internal typealias CompleteSignUpViewModelFactoryFactory =
        (@JvmSuppressWildcards SavedStateRegistryOwner, @JvmSuppressWildcards IncompleteUser) ->
        @JvmSuppressWildcards AbstractSavedStateViewModelFactory
