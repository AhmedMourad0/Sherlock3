package inc.ahmedmourad.sherlock.viewmodel.fragments.children

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.orNull
import arrow.core.right
import inc.ahmedmourad.sherlock.dagger.modules.factories.SherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.domain.constants.*
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.utils.exhaust
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.model.validators.children.*
import inc.ahmedmourad.sherlock.model.validators.common.validatePicturePath
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.utils.pickers.places.PlacePicker
import inc.ahmedmourad.sherlock.utils.toLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import splitties.init.appCtx

internal class AddChildViewModel(
        private val savedStateHandle: SavedStateHandle,
        private val serviceFactory: SherlockServiceIntentFactory,
        observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor
) : ViewModel() {

    val firstName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_FIRST_NAME, null) }
    val lastName: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_LAST_NAME, null) }
    val skin: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData(KEY_SKIN, null) }
    val hair: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData(KEY_HAIR, null) }
    val gender: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData(KEY_GENDER, null) }
    val location: LiveData<PlacePicker.Location?>
            by lazy { savedStateHandle.getLiveData(KEY_LOCATION, null) }
    val minAge: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData(KEY_MIN_AGE, null) }
    val maxAge: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData(KEY_MAX_AGE, null) }
    val minHeight: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData(KEY_MIN_HEIGHT, null) }
    val maxHeight: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData(KEY_MAX_HEIGHT, null) }
    val notes: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_NOTES, null) }
    val picturePath: LiveData<ImagePicker.PicturePath?>
            by lazy { savedStateHandle.getLiveData(KEY_PICTURE_PATH, null) }

    val firstNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_FIRST_NAME, null) }
    val lastNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_LAST_NAME, null) }
    val nameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_NAME, null) }
    val minAgeError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_MIN_AGE, null) }
    val maxAgeError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_MAX_AGE, null) }
    val locationError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_LOCATION, null) }
    val ageError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_AGE, null) }
    val minHeightError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_MIN_HEIGHT, null) }
    val maxHeightError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_MAX_HEIGHT, null) }
    val heightError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_HEIGHT, null) }
    val appearanceError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_APPEARANCE, null) }
    val picturePathError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_PICTURE_PATH, null) }
    val childError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData(KEY_ERROR_CHILD, null) }

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

    fun onFirstNameErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_FIRST_NAME, null)
    }

    fun onLastNameErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_LAST_NAME, null)
    }

    fun onNameErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_NAME, null)
    }

    fun onMinAgeErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_MIN_AGE, null)
    }

    fun onMaxAgeErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_MAX_AGE, null)
    }

    fun onLocationErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_LOCATION, null)
    }

    fun onAgeErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_AGE, null)
    }

    fun onMinHeightErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_MIN_HEIGHT, null)
    }

    fun onMaxHeightErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_MAX_HEIGHT, null)
    }

    fun onHeightErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_HEIGHT, null)
    }

    fun onAppearanceErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_APPEARANCE, null)
    }

    fun onPicturePathErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_PICTURE_PATH, null)
    }

    fun onChildErrorDismissed() {
        savedStateHandle.set(KEY_ERROR_CHILD, null)
    }

    val publishingState: LiveData<Either<Throwable, PublishingState>> = observeChildPublishingStateInteractor()
            .retry()
            .map<Either<Throwable, PublishingState>> { it.right() }
            .onErrorReturn { it.left() }
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()

    fun onPublish() {
        toPublishedChild()?.let {
            ContextCompat.startForegroundService(appCtx, serviceFactory(it))
        }
    }

    private fun toPublishedChild(): AppPublishedChild? {
        return Either.fx<Unit, AppPublishedChild> {

            val (firstName) = validateName(firstName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_FIRST_NAME, null)
            }

            val (lastName) = validateNameNullable(lastName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_LAST_NAME, null)
            }

            val (name) = validateNameEitherNullable(firstName, lastName).mapLeft {
                savedStateHandle.set(KEY_ERROR_NAME, null)
            }

            val (minAge) = validateAgeNullable(minAge.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MIN_AGE, null)
            }

            val (maxAge) = validateAgeNullable(maxAge.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MAX_AGE, null)
            }

            val (ageRange) = validateAgeRange(minAge, maxAge).mapLeft {
                savedStateHandle.set(KEY_ERROR_AGE, null)
            }

            val (minHeight) = validateHeightNullable(minHeight.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MIN_HEIGHT, null)
            }

            val (maxHeight) = validateHeightNullable(maxHeight.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_MAX_HEIGHT, null)
            }

            val (heightRange) = validateHeightRange(minHeight, maxHeight).mapLeft {
                savedStateHandle.set(KEY_ERROR_HEIGHT, null)
            }

            val (appearance) = validateApproximateAppearance(
                    ageRange,
                    heightRange,
                    gender.value?.let { findEnum(it, Gender.values()) },
                    skin.value?.let { findEnum(it, Skin.values()) },
                    hair.value?.let { findEnum(it, Hair.values()) }
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_APPEARANCE, null)
            }

            val coordinates = location.value?.let {
                validateCoordinates(it.latitude, it.longitude).mapLeft {
                    savedStateHandle.set(KEY_ERROR_LOCATION, null)
                }.bind()
            }

            val location = coordinates?.let { c ->
                location.value?.let {
                    validateLocation(
                            it.id,
                            it.name,
                            it.address,
                            c
                    ).mapLeft {
                        savedStateHandle.set(KEY_ERROR_LOCATION, null)
                    }.bind()
                }
            }

            val (picturePath) = picturePath.value?.let {
                validatePicturePath(it.value).mapLeft {
                    savedStateHandle.set(KEY_ERROR_PICTURE_PATH, null)
                }
            } ?: null.right()

            validateAppPublishedChild(
                    name,
                    notes.value,
                    location,
                    appearance,
                    picturePath
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_CHILD, null)
            }.bind()

        }.orNull()
    }

    class Factory(
            owner: SavedStateRegistryOwner,
            child: AppPublishedChild?,
            private val serviceFactory: SherlockServiceIntentFactory,
            private val observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs(child)) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return AddChildViewModel(
                    handle,
                    serviceFactory,
                    observeChildPublishingStateInteractor
            ) as T
        }
    }

    companion object {

        private const val KEY_FIRST_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.FIRST_NAME"
        private const val KEY_LAST_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.LAST_NAME"
        private const val KEY_SKIN =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.SKIN"
        private const val KEY_HAIR =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.HAIR"
        private const val KEY_GENDER =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.GENDER"
        private const val KEY_LOCATION =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.LOCATION"
        private const val KEY_MIN_AGE =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.MIN_AGE"
        private const val KEY_MAX_AGE =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.MAX_AGE"
        private const val KEY_MIN_HEIGHT =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.MIN_HEIGHT"
        private const val KEY_MAX_HEIGHT =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.MAX_HEIGHT"
        private const val KEY_NOTES =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.NOTES"
        private const val KEY_PICTURE_PATH =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.PICTURE_PATH"

        private const val KEY_ERROR_FIRST_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_FIRST_NAME"
        private const val KEY_ERROR_LAST_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_LAST_NAME"
        private const val KEY_ERROR_NAME =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_NAME"
        private const val KEY_ERROR_MIN_AGE =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_MIN_AGE"
        private const val KEY_ERROR_MAX_AGE =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_MAX_AGE"
        private const val KEY_ERROR_LOCATION =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_LOCATION"
        private const val KEY_ERROR_AGE =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_AGE"
        private const val KEY_ERROR_MIN_HEIGHT =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_MIN_HEIGHT"
        private const val KEY_ERROR_MAX_HEIGHT =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_MAX_HEIGHT"
        private const val KEY_ERROR_HEIGHT =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_HEIGHT"
        private const val KEY_ERROR_APPEARANCE =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_APPEARANCE"
        private const val KEY_ERROR_PICTURE_PATH =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_PICTURE_PATH"
        private const val KEY_ERROR_CHILD =
                "inc.ahmedmourad.sherlock.viewmodel.fragments.children.ERROR_CHILD"

        fun defaultArgs(child: AppPublishedChild?): Bundle? {
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

internal typealias AddChildViewModelFactoryFactory =
        (@JvmSuppressWildcards SavedStateRegistryOwner, @JvmSuppressWildcards AppPublishedChild?) ->
        @JvmSuppressWildcards AbstractSavedStateViewModelFactory
