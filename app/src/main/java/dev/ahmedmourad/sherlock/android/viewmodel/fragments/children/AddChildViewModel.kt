package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import arrow.core.toOption
import dagger.Lazy
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.children.AppChildToPublish
import dev.ahmedmourad.sherlock.android.model.validators.children.*
import dev.ahmedmourad.sherlock.android.model.validators.common.validatePicturePathNullable
import dev.ahmedmourad.sherlock.android.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.services.SherlockServiceIntentFactory
import dev.ahmedmourad.sherlock.android.utils.toLiveData
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.utils.exhaust
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import splitties.init.appCtx
import javax.inject.Inject
import javax.inject.Provider

internal class AddChildViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val serviceFactory: Lazy<SherlockServiceIntentFactory>,
        private val imageLoader: Lazy<ImageLoader>,
        bus: Lazy<Bus>
) : ViewModel() {

    val firstName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_FIRST_NAME, null) }
    val lastName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_LAST_NAME, null) }
    val skin: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_SKIN, null) }
    val hair: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_HAIR, null) }
    val gender: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_GENDER, null) }
    val location: LiveData<PlacePicker.Location?>
            by lazy { savedStateHandle.getLiveData<PlacePicker.Location?>(KEY_LOCATION, null) }
    val minAge: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_MIN_AGE, null) }
    val maxAge: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_MAX_AGE, null) }
    val minHeight: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_MIN_HEIGHT, null) }
    val maxHeight: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_MAX_HEIGHT, null) }
    val notes: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_NOTES, null) }
    val picturePath: LiveData<ImagePicker.PicturePath?>
            by lazy { savedStateHandle.getLiveData<ImagePicker.PicturePath?>(KEY_PICTURE_PATH, null) }

    val userError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_USER, null) }
    val firstNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_FIRST_NAME, null) }
    val lastNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_LAST_NAME, null) }
    val nameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_NAME, null) }
    val minAgeError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_MIN_AGE, null) }
    val maxAgeError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_MAX_AGE, null) }
    val locationError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_LOCATION, null) }
    val ageError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_AGE, null) }
    val minHeightError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_MIN_HEIGHT, null) }
    val maxHeightError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_MAX_HEIGHT, null) }
    val heightError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_HEIGHT, null) }
    val appearanceError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_APPEARANCE, null) }
    val picturePathError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_PICTURE_PATH, null) }
    val childError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_CHILD, null) }

    fun onFirstNameChange(newValue: String?) {
        savedStateHandle.set(KEY_FIRST_NAME, newValue)
    }

    fun onLastNameChange(newValue: String?) {
        savedStateHandle.set(KEY_LAST_NAME, newValue)
    }

    fun onSkinChange(newValue: Int?) {
        savedStateHandle.set(KEY_SKIN, newValue)
    }

    fun onHairChange(newValue: Int?) {
        savedStateHandle.set(KEY_HAIR, newValue)
    }

    fun onGenderChange(newValue: Int?) {
        savedStateHandle.set(KEY_GENDER, newValue)
    }

    fun onLocationChange(newValue: PlacePicker.Location?) {
        savedStateHandle.set(KEY_LOCATION, newValue)
    }

    fun onMinAgeChange(newValue: Int?) {
        savedStateHandle.set(KEY_MIN_AGE, newValue)
    }

    fun onMaxAgeChange(newValue: Int?) {
        savedStateHandle.set(KEY_MAX_AGE, newValue)
    }

    fun onMinHeightChange(newValue: Int?) {
        savedStateHandle.set(KEY_MIN_HEIGHT, newValue)
    }

    fun onMaxHeightChange(newValue: Int?) {
        savedStateHandle.set(KEY_MAX_HEIGHT, newValue)
    }

    fun onNotesChange(newValue: String?) {
        savedStateHandle.set(KEY_NOTES, newValue)
    }

    fun onPicturePathChange(newValue: ImagePicker.PicturePath?) {
        savedStateHandle.set(KEY_PICTURE_PATH, newValue)
    }

    fun onUserErrorHandled() {
        savedStateHandle.set(KEY_ERROR_USER, null)
    }

    fun onFirstNameErrorHandled() {
        savedStateHandle.set(KEY_ERROR_FIRST_NAME, null)
    }

    fun onLastNameErrorHandled() {
        savedStateHandle.set(KEY_ERROR_LAST_NAME, null)
    }

    fun onNameErrorHandled() {
        savedStateHandle.set(KEY_ERROR_NAME, null)
    }

    fun onMinAgeErrorHandled() {
        savedStateHandle.set(KEY_ERROR_MIN_AGE, null)
    }

    fun onMaxAgeErrorHandled() {
        savedStateHandle.set(KEY_ERROR_MAX_AGE, null)
    }

    fun onLocationErrorHandled() {
        savedStateHandle.set(KEY_ERROR_LOCATION, null)
    }

    fun onAgeErrorHandled() {
        savedStateHandle.set(KEY_ERROR_AGE, null)
    }

    fun onMinHeightErrorHandled() {
        savedStateHandle.set(KEY_ERROR_MIN_HEIGHT, null)
    }

    fun onMaxHeightErrorHandled() {
        savedStateHandle.set(KEY_ERROR_MAX_HEIGHT, null)
    }

    fun onHeightErrorHandled() {
        savedStateHandle.set(KEY_ERROR_HEIGHT, null)
    }

    fun onAppearanceErrorHandled() {
        savedStateHandle.set(KEY_ERROR_APPEARANCE, null)
    }

    fun onPicturePathErrorHandled() {
        savedStateHandle.set(KEY_ERROR_PICTURE_PATH, null)
    }

    fun onChildErrorHandled() {
        savedStateHandle.set(KEY_ERROR_CHILD, null)
    }

    val publishingState by lazy {
        bus.get()
                .childPublishingState
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .toFlowable(BackpressureStrategy.BUFFER)
                .toLiveData()
    }

    fun onPublish(user: SignedInUser?) {
        toPublishedChild(user)?.let {
            ContextCompat.startForegroundService(appCtx, serviceFactory.get().invoke(it))
        }
    }

    private fun toPublishedChild(user: SignedInUser?): AppChildToPublish? {
        return Either.fx<Unit, AppChildToPublish> {

            val u = !user.toOption().toEither {
                savedStateHandle.set(KEY_ERROR_USER, appCtx.getString(R.string.authentication_needed))
            }

            val firstName = !validateNameNullable(firstName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_FIRST_NAME, it)
            }

            val lastName = !validateNameNullable(lastName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_LAST_NAME, it)
            }

            val name = !validateNameEitherNullable(firstName, lastName).mapLeft {
                savedStateHandle.set(KEY_ERROR_NAME, it)
            }

            val minAge = !validateAgeNullable(minAge.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MIN_AGE, it)
            }

            val maxAge = !validateAgeNullable(maxAge.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MAX_AGE, it)
            }

            val ageRange = !validateAgeRangeNullable(minAge, maxAge).mapLeft {
                savedStateHandle.set(KEY_ERROR_AGE, it)
            }

            val minHeight = !validateHeightNullable(minHeight.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MIN_HEIGHT, it)
            }

            val maxHeight = !validateHeightNullable(maxHeight.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MAX_HEIGHT, it)
            }

            val heightRange = !validateHeightRangeNullable(minHeight, maxHeight).mapLeft {
                savedStateHandle.set(KEY_ERROR_HEIGHT, it)
            }

            val appearance = !validateApproximateAppearance(
                    ageRange,
                    heightRange,
                    gender.value?.let { findEnum(it, Gender.values()) },
                    skin.value?.let { findEnum(it, Skin.values()) },
                    hair.value?.let { findEnum(it, Hair.values()) }
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_APPEARANCE, it)
            }

            val coordinates = !validateCoordinatesNullable(
                    location.value?.latitude,
                    location.value?.longitude
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_LOCATION, it)
            }

            val location = !validateLocationNullable(
                    location.value?.id,
                    location.value?.name,
                    location.value?.address,
                    coordinates
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_LOCATION, it)
            }

            val picturePath = !validatePicturePathNullable(picturePath.value?.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_PICTURE_PATH, it)
            }

            validateAppPublishedChild(
                    u.simplify(),
                    name,
                    notes.value,
                    location,
                    appearance,
                    picturePath,
                    imageLoader.get()
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_CHILD, it)
            }.bind()

        }.orNull()
    }

    @Reusable
    class Factory @Inject constructor(
            private val serviceFactory: Provider<Lazy<SherlockServiceIntentFactory>>,
            private val imageLoader: Provider<Lazy<ImageLoader>>,
            private val bus: Provider<Lazy<Bus>>
    ) : AssistedViewModelFactory<AddChildViewModel> {
        override fun invoke(handle: SavedStateHandle): AddChildViewModel {
            return AddChildViewModel(
                    handle,
                    serviceFactory.get(),
                    imageLoader.get(),
                    bus.get()
            )
        }
    }

    companion object {

        private const val KEY_FIRST_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.FIRST_NAME"
        private const val KEY_LAST_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.LAST_NAME"
        private const val KEY_SKIN =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.SKIN"
        private const val KEY_HAIR =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.HAIR"
        private const val KEY_GENDER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.GENDER"
        private const val KEY_LOCATION =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.LOCATION"
        private const val KEY_MIN_AGE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.MIN_AGE"
        private const val KEY_MAX_AGE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.MAX_AGE"
        private const val KEY_MIN_HEIGHT =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.MIN_HEIGHT"
        private const val KEY_MAX_HEIGHT =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.MAX_HEIGHT"
        private const val KEY_NOTES =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.NOTES"
        private const val KEY_PICTURE_PATH =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.PICTURE_PATH"

        private const val KEY_ERROR_USER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_USER"
        private const val KEY_ERROR_FIRST_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_FIRST_NAME"
        private const val KEY_ERROR_LAST_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_LAST_NAME"
        private const val KEY_ERROR_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_NAME"
        private const val KEY_ERROR_MIN_AGE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_MIN_AGE"
        private const val KEY_ERROR_MAX_AGE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_MAX_AGE"
        private const val KEY_ERROR_LOCATION =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_LOCATION"
        private const val KEY_ERROR_AGE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_AGE"
        private const val KEY_ERROR_MIN_HEIGHT =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_MIN_HEIGHT"
        private const val KEY_ERROR_MAX_HEIGHT =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_MAX_HEIGHT"
        private const val KEY_ERROR_HEIGHT =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_HEIGHT"
        private const val KEY_ERROR_APPEARANCE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_APPEARANCE"
        private const val KEY_ERROR_PICTURE_PATH =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_PICTURE_PATH"
        private const val KEY_ERROR_CHILD =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_CHILD"

        fun defaultArgs(child: AppChildToPublish?): Bundle? {
            return child?.let { c ->
                Bundle(12).apply {

                    when (val name = child.name) {

                        is Either.Left -> {
                            putString(KEY_FIRST_NAME, name.a.value)
                            putString(KEY_LAST_NAME, null)
                        }

                        is Either.Right -> {
                            putString(KEY_FIRST_NAME, name.b.first.value)
                            putString(KEY_LAST_NAME, name.b.last.value)
                        }

                        null -> {
                            putString(KEY_FIRST_NAME, null)
                            putString(KEY_LAST_NAME, null)
                        }
                    }.exhaust()

                    c.appearance.skin?.value?.let { putInt(KEY_SKIN, it) }
                    c.appearance.hair?.value?.let { putInt(KEY_HAIR, it) }
                    c.appearance.gender?.value?.let { putInt(KEY_GENDER, it) }

                    c.appearance.ageRange?.min?.value?.let { putInt(KEY_MIN_AGE, it) }
                    c.appearance.ageRange?.max?.value?.let { putInt(KEY_MAX_AGE, it) }

                    c.appearance.heightRange?.min?.value?.let { putInt(KEY_MIN_HEIGHT, it) }
                    c.appearance.heightRange?.max?.value?.let { putInt(KEY_MAX_HEIGHT, it) }

                    putParcelable(KEY_LOCATION, c.location?.let(PlacePicker.Location.Companion::from))
                    putParcelable(KEY_PICTURE_PATH, c.picturePath?.let(ImagePicker.PicturePath.Companion::from))

                    putString(KEY_NOTES, c.notes)
                }
            }
        }
    }
}
