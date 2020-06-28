package dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.orNull
import arrow.core.right
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.bundlizer.bundle
import dev.ahmedmourad.bundlizer.unbundle
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.auth.AppCompletedUser
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateAppCompletedUser
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateDisplayName
import dev.ahmedmourad.sherlock.android.model.validators.auth.validateEmail
import dev.ahmedmourad.sherlock.android.model.validators.auth.validatePhoneNumber
import dev.ahmedmourad.sherlock.android.model.validators.common.validatePicturePath
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.CompleteSignUpInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import javax.inject.Provider

internal class CompleteSignUpViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val completeSignUpInteractor: Lazy<CompleteSignUpInteractor>,
        private val imageLoader: Lazy<ImageLoader>
) : ViewModel() {

    private val id: UserId = savedStateHandle.get<Bundle>(KEY_ID)!!.unbundle(UserId.serializer())

    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_EMAIL, null) }
    val displayName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_DISPLAY_NAME, null) }
    val phoneNumberCountryCode: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PHONE_NUMBER_COUNTRY_CODE, null) }
    val phoneNumber: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_PHONE_NUMBER, null) }
    val picture: LiveData<ImagePicker.PicturePath?>
            by lazy { savedStateHandle.getLiveData<ImagePicker.PicturePath?>(KEY_PICTURE, null) }

    val emailError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_EMAIL, null) }
    val displayNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_DISPLAY_NAME, null) }
    val phoneNumberError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PHONE_NUMBER, null) }
    val pictureError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PICTURE, null) }
    val userError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_USER, null) }

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
        savedStateHandle.set(KEY_PICTURE, newValue)
    }

    fun onEmailErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onDisplayNameErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_DISPLAY_NAME, null)
    }

    fun onPhoneNumberErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, null)
    }

    fun onPicturePathErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PICTURE, null)
    }

    fun onUserErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_USER, null)
    }

    fun onCompleteSignUp(): Single<Either<CompleteSignUpInteractor.Exception, SignedInUser>>? {
        return toCompletedUser()?.let {
            completeSignUpInteractor.get().invoke(
                    it.toCompletedUser(imageLoader.get())
            ).observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun toCompletedUser(): AppCompletedUser? {
        return Either.fx<Unit, AppCompletedUser> {

            val email = !validateEmail(email.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_EMAIL, it)
            }

            val displayName = !validateDisplayName(displayName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_DISPLAY_NAME, it)
            }

            val phoneNumber = !validatePhoneNumber(
                    phoneNumberCountryCode.value,
                    phoneNumber.value
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, it)
            }

            val picture = picture.value?.let { pic ->
                Url.of(pic.value).fold(ifLeft = {
                    validatePicturePath(pic.value).bimap(leftOperation = {
                        savedStateHandle.set(KEY_ERROR_PICTURE, it)
                    }, rightOperation = PicturePath::right)
                }, ifRight = {
                    it.left().right()
                })
            }?.bind()

            validateAppCompletedUser(
                    id,
                    email,
                    displayName,
                    phoneNumber,
                    picture
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_USER, it)
            }.bind()

        }.orNull()
    }

    @Reusable
    class Factory @Inject constructor(
            private val completeSignUpInteractor: Provider<Lazy<CompleteSignUpInteractor>>,
            private val imageLoader: Provider<Lazy<ImageLoader>>
    ) : AssistedViewModelFactory<CompleteSignUpViewModel> {
        override fun invoke(handle: SavedStateHandle): CompleteSignUpViewModel {
            return CompleteSignUpViewModel(
                    handle,
                    completeSignUpInteractor.get(),
                    imageLoader.get()
            )
        }
    }

    companion object {

        private const val KEY_ID =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ID"

        private const val KEY_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.EMAIL"
        private const val KEY_DISPLAY_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.DISPLAY_NAME"
        private const val KEY_PHONE_NUMBER_COUNTRY_CODE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PHONE_NUMBER_COUNTRY_CODE"
        private const val KEY_PHONE_NUMBER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PHONE_NUMBER"
        private const val KEY_PICTURE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.PICTURE"

        private const val KEY_ERROR_EMAIL =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_EMAIL"
        private const val KEY_ERROR_DISPLAY_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_DISPLAY_NAME"
        private const val KEY_ERROR_PHONE_NUMBER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_PHONE_NUMBER"
        private const val KEY_ERROR_PICTURE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_PICTURE"
        private const val KEY_ERROR_USER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.ERROR_USER"

        fun defaultArgs(incompleteUser: IncompleteUser): Bundle? {
            return Bundle(6).apply {
                putBundle(KEY_ID, incompleteUser.id.bundle(UserId.serializer()))
                putString(KEY_EMAIL, incompleteUser.email?.value)
                putString(KEY_DISPLAY_NAME, incompleteUser.displayName?.value)
                putString(KEY_PHONE_NUMBER_COUNTRY_CODE, incompleteUser.phoneNumber?.countryCode)
                putString(KEY_PHONE_NUMBER, incompleteUser.phoneNumber?.number)
                putParcelable(
                        KEY_PICTURE,
                        incompleteUser.pictureUrl?.let(ImagePicker.PicturePath.Companion::from)
                )
            }
        }
    }
}
