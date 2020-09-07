package dev.ahmedmourad.sherlock.android.viewmodel.fragments.children

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import arrow.core.toOption
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.model.validators.children.*
import dev.ahmedmourad.sherlock.android.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.android.viewmodel.factory.AssistedViewModelFactory
import dev.ahmedmourad.sherlock.domain.model.auth.SignedInUser
import dev.ahmedmourad.sherlock.domain.model.children.ChildrenQuery
import javax.inject.Inject

internal class FindChildrenViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

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
    val age: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_AGE, null) }
    val height: LiveData<Int?>
            by lazy { savedStateHandle.getLiveData<Int?>(KEY_HEIGHT, null) }

    val userError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_USER, null) }
    val firstNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_FIRST_NAME, null) }
    val lastNameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_LAST_NAME, null) }
    val nameError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_NAME, null) }
    val locationError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_LOCATION, null) }
    val ageError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_AGE, null) }
    val heightError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_HEIGHT, null) }
    val genderError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_GENDER, null) }
    val skinError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_SKIN, null) }
    val hairError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_HAIR, null) }
    val appearanceError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_APPEARANCE, null) }
    val queryError: LiveData<String?>
            by lazy { savedStateHandle.getLiveData<String?>(KEY_ERROR_QUERY, null) }

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

    fun onAgeChange(newValue: Int?) {
        savedStateHandle.set(KEY_AGE, newValue)
    }

    fun onHeightChange(newValue: Int?) {
        savedStateHandle.set(KEY_HEIGHT, newValue)
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

    fun onLocationErrorHandled() {
        savedStateHandle.set(KEY_ERROR_LOCATION, null)
    }

    fun onAgeErrorHandled() {
        savedStateHandle.set(KEY_ERROR_AGE, null)
    }

    fun onHeightErrorHandled() {
        savedStateHandle.set(KEY_ERROR_HEIGHT, null)
    }

    fun onGenderErrorHandled() {
        savedStateHandle.set(KEY_ERROR_GENDER, null)
    }

    fun onSkinErrorHandled() {
        savedStateHandle.set(KEY_ERROR_SKIN, null)
    }

    fun onHairErrorHandled() {
        savedStateHandle.set(KEY_ERROR_HAIR, null)
    }

    fun onAppearanceErrorHandled() {
        savedStateHandle.set(KEY_ERROR_APPEARANCE, null)
    }

    fun onQueryErrorHandled() {
        savedStateHandle.set(KEY_ERROR_QUERY, null)
    }


    fun toChildQuery(user: SignedInUser?): ChildrenQuery? {
        return Either.fx<Unit, ChildrenQuery> {

            val u = !user.toOption().toEither {
                savedStateHandle.set(KEY_ERROR_USER, "This action requires authentication")
            }

            val firstName = !validateName(firstName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_FIRST_NAME, it)
            }

            val lastName = !validateName(lastName.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_LAST_NAME, it)
            }

            val fullName = !validateFullName(firstName, lastName).mapLeft {
                savedStateHandle.set(KEY_ERROR_NAME, it)
            }

            val age = !validateAge(age.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_AGE, it)
            }

            val height = !validateHeight(height.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_HEIGHT, it)
            }

            val gender = !validateGender(gender.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_GENDER, it)
            }

            val skin = !validateSkin(skin.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_SKIN, it)
            }

            val hair = !validateHair(hair.value).mapLeft {
                savedStateHandle.set(KEY_ERROR_HAIR, it)
            }

            val appearance = !validateExactAppearance(
                    age,
                    height,
                    gender,
                    skin,
                    hair
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_APPEARANCE, it)
            }

            val coordinatesNullable = location.value?.let { l ->
                validateCoordinates(l.latitude, l.longitude).mapLeft {
                    savedStateHandle.set(KEY_ERROR_LOCATION, it)
                }.bind()
            }

            val coordinates = !validateCoordinates(coordinatesNullable).mapLeft {
                savedStateHandle.set(KEY_ERROR_LOCATION, it)
            }

            validateChildQuery(
                    u.simplify(),
                    fullName,
                    coordinates,
                    appearance,
                    0 //TODO: pagination
            ).mapLeft {
                savedStateHandle.set(KEY_ERROR_QUERY, it)
            }.bind()

        }.orNull()
    }

    @Reusable
    class Factory @Inject constructor() : AssistedViewModelFactory<FindChildrenViewModel> {
        override fun invoke(handle: SavedStateHandle): FindChildrenViewModel {
            return FindChildrenViewModel(handle)
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
        private const val KEY_AGE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.AGE"
        private const val KEY_HEIGHT =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.HEIGHT"

        private const val KEY_ERROR_USER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_USER"
        private const val KEY_ERROR_FIRST_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_FIRST_NAME"
        private const val KEY_ERROR_LAST_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_LAST_NAME"
        private const val KEY_ERROR_NAME =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_NAME"
        private const val KEY_ERROR_LOCATION =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_LOCATION"
        private const val KEY_ERROR_AGE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_AGE"
        private const val KEY_ERROR_HEIGHT =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_HEIGHT"
        private const val KEY_ERROR_GENDER =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_GENDER"
        private const val KEY_ERROR_SKIN =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_SKIN"
        private const val KEY_ERROR_HAIR =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_HAIR"
        private const val KEY_ERROR_APPEARANCE =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_APPEARANCE"
        private const val KEY_ERROR_QUERY =
                "dev.ahmedmourad.sherlock.android.viewmodel.fragments.children.ERROR_QUERY"

        fun defaultArgs(): Bundle? = null
    }
}
