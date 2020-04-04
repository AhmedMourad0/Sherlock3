package inc.ahmedmourad.sherlock.viewmodel.fragments.children

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.orNull
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.validators.children.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers

internal class FindChildrenViewModel(
        observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor
) : ViewModel() {

    val firstName by lazy { MutableLiveData<String?>("") }
    val lastName by lazy { MutableLiveData<String?>("") }
    val skin by lazy { MutableLiveData<Skin?>(Skin.WHITE) }
    val hair by lazy { MutableLiveData<Hair?>(Hair.BLONDE) }
    val gender by lazy { MutableLiveData<Gender?>(Gender.MALE) }
    val location by lazy { MutableLiveData<Location?>() }
    val age by lazy { MutableLiveData<Int?>(15) }
    val height by lazy { MutableLiveData<Int?>(120) }

    val firstNameError by lazy { MutableLiveData<String?>() }
    val lastNameError by lazy { MutableLiveData<String?>() }
    val nameError by lazy { MutableLiveData<String?>() }
    val locationError by lazy { MutableLiveData<String?>() }
    val ageError by lazy { MutableLiveData<String?>() }
    val heightError by lazy { MutableLiveData<String?>() }
    val genderError by lazy { MutableLiveData<String?>() }
    val skinError by lazy { MutableLiveData<String?>() }
    val hairError by lazy { MutableLiveData<String?>() }
    val appearanceError by lazy { MutableLiveData<String?>() }
    val queryError by lazy { MutableLiveData<String?>() }

    val internetConnectivityFlowable: Flowable<Boolean> = observeInternetConnectivityInteractor()
            .retry()
            .observeOn(AndroidSchedulers.mainThread())

    fun toChildQuery(): ChildQuery? {
        return Either.fx<Unit, ChildQuery> {

            val (firstName) = validateName(firstName.value).mapLeft(firstNameError::setValue)

            val (lastName) = validateName(lastName.value).mapLeft(lastNameError::setValue)

            val (fullName) = validateFullName(firstName, lastName).mapLeft(nameError::setValue)

            val (age) = validateAge(age.value).mapLeft(ageError::setValue)

            val (height) = validateHeight(height.value).mapLeft(heightError::setValue)

            val (gender) = validateGender(this@FindChildrenViewModel.gender.value).mapLeft(genderError::setValue)

            val (skin) = validateSkin(this@FindChildrenViewModel.skin.value).mapLeft(skinError::setValue)

            val (hair) = validateHair(this@FindChildrenViewModel.hair.value).mapLeft(hairError::setValue)

            val (appearance) = validateExactAppearance(
                    age,
                    height,
                    gender,
                    skin,
                    hair
            ).mapLeft(appearanceError::setValue)

            val (location) = validateLocation(location.value).mapLeft(locationError::setValue)

            validateChildQuery(
                    fullName,
                    location,
                    appearance
            ).mapLeft(queryError::setValue).bind()

        }.orNull()
    }
}
