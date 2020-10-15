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
import dev.ahmedmourad.sherlock.android.model.auth.AppCompletedUser
import dev.ahmedmourad.sherlock.android.model.validators.auth.*
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.interactors.auth.CompleteSignUpInteractor
import dev.ahmedmourad.sherlock.domain.model.auth.IncompleteUser
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import dev.ahmedmourad.sherlock.domain.utils.disposable
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

internal class CompleteSignUpViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val completeSignUpInteractor: Lazy<CompleteSignUpInteractor>,
        private val imageLoader: Lazy<ImageLoader>
) : ViewModel() {

    private var disposable by disposable()

    private val id: UserId = savedStateHandle.get<Bundle>(KEY_ID)!!.unbundle(UserId.serializer())

    val email: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_EMAIL, null) }
    val displayName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_DISPLAY_NAME, null) }
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

    val completeSignUpState: LiveData<CompleteSignUpState?> by lazy {
        Transformations.map(savedStateHandle.getLiveData<Bundle?>(
                KEY_COMPLETE_SIGN_UP_STATE,
                null
        )) {
            it?.unbundle(CompleteSignUpState.serializer())
        }
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
        savedStateHandle.set(KEY_PICTURE, newValue)
    }

    fun onEmailErrorHandled() {
        savedStateHandle.set(KEY_ERROR_EMAIL, null)
    }

    fun onDisplayNameErrorHandled() {
        savedStateHandle.set(KEY_ERROR_DISPLAY_NAME, null)
    }

    fun onPhoneNumberErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, null)
    }

    fun onPicturePathErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PICTURE, null)
    }

    fun onUserErrorHandled() {
        savedStateHandle.set(KEY_ERROR_USER, null)
    }

    fun onCompleteSignUpStateHandled() {
        savedStateHandle.set(KEY_COMPLETE_SIGN_UP_STATE, null)
    }

    fun onCompleteSignUp() {
        toCompletedUser()?.let { appUser ->
            disposable = completeSignUpInteractor.get()
                    .invoke(appUser.toCompletedUser(imageLoader.get()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ either ->
                        either.fold(ifLeft = { e ->
                            when (e) {

                                CompleteSignUpInteractor.Exception.NoInternetConnectionException -> {
                                    savedStateHandle.set(
                                            KEY_COMPLETE_SIGN_UP_STATE,
                                            CompleteSignUpState.NoInternet.bundle(CompleteSignUpState.serializer())
                                    )
                                }

                                CompleteSignUpInteractor.Exception.NoSignedInUserException -> {
                                    savedStateHandle.set(
                                            KEY_COMPLETE_SIGN_UP_STATE,
                                            CompleteSignUpState.NoSignedInUser.bundle(CompleteSignUpState.serializer())
                                    )
                                }

                                is CompleteSignUpInteractor.Exception.InternalException -> {
                                    Timber.error(e.origin, e.origin::toString)
                                    savedStateHandle.set(
                                            KEY_COMPLETE_SIGN_UP_STATE,
                                            CompleteSignUpState.Error.bundle(CompleteSignUpState.serializer())
                                    )
                                }

                                is CompleteSignUpInteractor.Exception.UnknownException -> {
                                    Timber.error(e.origin, e.origin::toString)
                                    savedStateHandle.set(
                                            KEY_COMPLETE_SIGN_UP_STATE,
                                            CompleteSignUpState.Error.bundle(CompleteSignUpState.serializer())
                                    )
                                }
                            }.exhaust()
                        }, ifRight = {
                            savedStateHandle.set(
                                    KEY_COMPLETE_SIGN_UP_STATE,
                                    CompleteSignUpState.Success(it).bundle(CompleteSignUpState.serializer())
                            )
                        })
                    }, {
                        Timber.error(it, it::toString)
                        savedStateHandle.set(
                                KEY_COMPLETE_SIGN_UP_STATE,
                                CompleteSignUpState.Error.bundle(CompleteSignUpState.serializer())
                        )
                    })
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

            val phoneNumber = !validatePhoneNumber(phoneNumber.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_PHONE_NUMBER, it)
            }

            val picture = !validatePictureEitherNullable(picture.value?.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_PICTURE, it)
            }

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

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

    @Serializable
    sealed class CompleteSignUpState {

        @Serializable
        data class Success(val user: SignedInUser) : CompleteSignUpState()

        @Serializable
        object NoSignedInUser : CompleteSignUpState()

        @Serializable
        object NoInternet : CompleteSignUpState()

        @Serializable
        object Error : CompleteSignUpState()
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

        private const val KEY_COMPLETE_SIGN_UP_STATE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.auth.key.COMPLETE_SIGN_UP_STATE"

        fun defaultArgs(incompleteUser: IncompleteUser): Bundle? {
            return Bundle(6).apply {
                putBundle(KEY_ID, incompleteUser.id.bundle(UserId.serializer()))
                putString(KEY_EMAIL, incompleteUser.email?.value)
                putString(KEY_DISPLAY_NAME, incompleteUser.displayName?.value)
                putString(KEY_PHONE_NUMBER, incompleteUser.phoneNumber?.fullNumber())
                putParcelable(
                        KEY_PICTURE,
                        incompleteUser.pictureUrl?.let(ImagePicker.PicturePath.Companion::from)
                )
            }
        }
    }
}
