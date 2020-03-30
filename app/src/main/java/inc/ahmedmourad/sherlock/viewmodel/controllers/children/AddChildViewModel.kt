package inc.ahmedmourad.sherlock.viewmodel.controllers.children

import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.extensions.fx
import arrow.core.orNull
import arrow.core.toT
import inc.ahmedmourad.sherlock.dagger.modules.factories.SherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.PublishingState
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.interactors.common.CheckChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.CheckInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.common.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import inc.ahmedmourad.sherlock.domain.model.common.PicturePath
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.viewmodel.controllers.validators.children.*
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import splitties.init.appCtx

internal class AddChildViewModel(
        private val serviceFactory: SherlockServiceIntentFactory,
        observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        checkInternetConnectivityInteractor: CheckInternetConnectivityInteractor,
        observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor,
        checkChildPublishingStateInteractor: CheckChildPublishingStateInteractor
) : ViewModel() {

    val firstName by lazy { MutableLiveData<String?>("") }
    val lastName by lazy { MutableLiveData<String?>("") }
    val skin by lazy { MutableLiveData<Skin?>(Skin.WHITE) }
    val hair by lazy { MutableLiveData<Hair?>(Hair.BLONDE) }
    val gender by lazy { MutableLiveData<Gender?>(Gender.MALE) }
    val location by lazy { MutableLiveData<Location?>() }
    val minAge by lazy { MutableLiveData<Int?>(0) }
    val maxAge by lazy { MutableLiveData<Int?>(200) }
    val minHeight by lazy { MutableLiveData<Int?>(20) }
    val maxHeight by lazy { MutableLiveData<Int?>(300) }
    val notes by lazy { MutableLiveData<String?>() }
    val picturePath by lazy { MutableLiveData<PicturePath?>() }

    val firstNameError by lazy { MutableLiveData<String?>() }
    val lastNameError by lazy { MutableLiveData<String?>() }
    val nameError by lazy { MutableLiveData<String?>() }
    val minAgeError by lazy { MutableLiveData<String?>() }
    val maxAgeError by lazy { MutableLiveData<String?>() }
    val ageError by lazy { MutableLiveData<String?>() }
    val minHeightError by lazy { MutableLiveData<String?>() }
    val maxHeightError by lazy { MutableLiveData<String?>() }
    val heightError by lazy { MutableLiveData<String?>() }
    val appearanceError by lazy { MutableLiveData<String?>() }
    val childError by lazy { MutableLiveData<String?>() }

    val internetConnectivityFlowable: Flowable<Tuple2<Boolean, PublishingState?>> =
            observeInternetConnectivityInteractor()
                    .retry()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMapSingle { isConnected ->
                        checkChildPublishingStateInteractor()
                                .observeOn(AndroidSchedulers.mainThread())
                                .map { isConnected toT it.orNull() }
                    }

    val internetConnectivitySingle: Single<Boolean> = checkInternetConnectivityInteractor()
            .observeOn(AndroidSchedulers.mainThread())

    val publishingStateFlowable: Flowable<PublishingState> = observeChildPublishingStateInteractor()
            .retry()
            .observeOn(AndroidSchedulers.mainThread())

    fun onPublish() {
        toPublishedChild()?.let {
            ContextCompat.startForegroundService(appCtx, serviceFactory(it))
        }
    }

    fun take(child: AppPublishedChild) {

        when (val name = child.name) {

            is Either.Left -> {
                firstName.value = name.a.value
                lastName.value = null
            }

            is Either.Right -> {
                firstName.value = name.b.first.value
                lastName.value = name.b.last.value
            }

            else -> {
                firstName.value = null
                lastName.value = null
            }
        }

        skin.value = child.appearance.skin
        hair.value = child.appearance.hair
        gender.value = child.appearance.gender
        location.value = child.location
        minAge.value = child.appearance.ageRange?.min?.value
        maxAge.value = child.appearance.ageRange?.max?.value
        minHeight.value = child.appearance.heightRange?.min?.value
        maxHeight.value = child.appearance.heightRange?.max?.value
        notes.value = child.notes
        notes.value = child.notes
        picturePath.value = child.picturePath
    }

    private fun toPublishedChild(): AppPublishedChild? {
        return Either.fx<Unit, AppPublishedChild> {

            val (firstName) = validateName(firstName.value).mapLeft(firstNameError::setValue)

            val (lastName) = validateNameNullable(lastName.value).mapLeft(lastNameError::setValue)

            val (name) = validateNameEitherNullable(firstName, lastName).mapLeft(nameError::setValue)

            val (minAge) = validateAgeNullable(minAge.value).mapLeft(minAgeError::setValue)

            val (maxAge) = validateAgeNullable(maxAge.value).mapLeft(maxAgeError::setValue)

            val (ageRange) = validateAgeRange(minAge, maxAge).mapLeft(ageError::setValue)

            val (minHeight) = validateHeightNullable(minHeight.value).mapLeft(minHeightError::setValue)

            val (maxHeight) = validateHeightNullable(maxHeight.value).mapLeft(maxHeightError::setValue)

            val (heightRange) = validateHeightRange(minHeight, maxHeight).mapLeft(heightError::setValue)

            val (appearance) = validateApproximateAppearance(
                    ageRange,
                    heightRange,
                    gender.value,
                    skin.value,
                    hair.value
            ).mapLeft(appearanceError::setValue)

            validateAppPublishedChild(
                    name,
                    notes.value,
                    location.value,
                    appearance,
                    picturePath.value
            ).mapLeft(childError::setValue).bind()

        }.orNull()
    }
}
