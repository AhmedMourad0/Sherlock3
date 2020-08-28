package dev.ahmedmourad.sherlock.android.model.validators.children

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.android.interpreters.model.localizedMessage
import dev.ahmedmourad.sherlock.android.loader.ImageLoader
import dev.ahmedmourad.sherlock.android.model.children.AppChildToPublish
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.ChildQuery
import dev.ahmedmourad.sherlock.domain.model.children.ChildToPublish
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import splitties.init.appCtx

internal fun validateFullName(
        firstName: Name,
        lastName: Name
): Either<String, FullName> {
    return FullName.of(firstName, lastName).right()
}

internal fun validateNameEitherNullable(
        firstName: Name?,
        lastName: Name?
): Either<String, Either<Name, FullName>?> {

    if (firstName == null && lastName != null) {
        return appCtx.getString(R.string.first_name_missing).left()
    }

    return when {
        firstName == null -> null.right()
        lastName == null -> firstName.left().right()
        else -> validateFullName(firstName, lastName).map(FullName::right)
    }
}

internal fun validateName(value: String?): Either<String, Name> {

    if (value == null) {
        return appCtx.getString(R.string.name_empty_or_blank).left()
    }

    return Name.of(value).mapLeft(Name.Exception::localizedMessage)
}

internal fun validateNameNullable(value: String?): Either<String, Name?> {
    return value?.let(::validateName) ?: null.right()
}

internal fun validateAgeRange(
        minAge: Age?,
        maxAge: Age?
): Either<String, AgeRange?> {

    if ((minAge == null) != (maxAge == null)) {
        return appCtx.getString(R.string.invalid_age_range).left()
    }

    if (minAge != null && maxAge != null) {
        return AgeRange.of(minAge, maxAge).mapLeft(AgeRange.Exception::localizedMessage)
    }

    return null.right()
}

internal fun validateAge(value: Int?): Either<String, Age> {

    if (value == null) {
        return appCtx.getString(R.string.invalid_age).left()
    }

    return Age.of(value).mapLeft(Age.Exception::localizedMessage)
}

internal fun validateAgeNullable(value: Int?): Either<String, Age?> {
    return if (value != null) {
        validateAge(value)
    } else {
        null.right()
    }
}

internal fun validateHeightRange(
        minHeight: Height?,
        maxHeight: Height?
): Either<String, HeightRange?> {

    if ((minHeight == null) != (maxHeight == null)) {
        return appCtx.getString(R.string.invalid_height_range).left()
    }

    if (minHeight != null && maxHeight != null) {
        return HeightRange.of(minHeight, maxHeight).mapLeft(HeightRange.Exception::localizedMessage)
    }

    return null.right()
}

internal fun validateHeight(value: Int?): Either<String, Height> {

    if (value == null) {
        return appCtx.getString(R.string.invalid_height).left()
    }

    return Height.of(value).mapLeft(Height.Exception::localizedMessage)
}

internal fun validateHeightNullable(value: Int?): Either<String, Height?> {
    return if (value != null) {
        validateHeight(value)
    } else {
        null.right()
    }
}

internal fun validateGender(gender: Int?): Either<String, Gender> {

    if (gender == null) {
        return appCtx.getString(R.string.gender_missing).left()
    }

    return Gender.values().firstOrNull { it.value == gender }?.right()
            ?: appCtx.getString(R.string.gender_missing).left()
}

internal fun validateSkin(skin: Int?): Either<String, Skin> {

    if (skin == null) {
        return appCtx.getString(R.string.skin_color_missing).left()
    }

    return Skin.values().firstOrNull { it.value == skin }?.right()
            ?: appCtx.getString(R.string.skin_color_missing).left()
}

internal fun validateHair(hair: Int?): Either<String, Hair> {

    if (hair == null) {
        return appCtx.getString(R.string.hair_color_missing).left()
    }

    return Hair.values().firstOrNull { it.value == hair }?.right()
            ?: appCtx.getString(R.string.hair_color_missing).left()
}

internal fun validateCoordinates(latitude: Double?, longitude: Double?): Either<String, Coordinates?> {
    latitude ?: return null.right()
    longitude ?: return null.right()
    return Coordinates.of(latitude, longitude).mapLeft(Coordinates.Exception::localizedMessage)
}

internal fun validateLocation(location: Location?): Either<String, Location> {
    return location?.right() ?: appCtx.getString(R.string.invalid_last_known_location).left()
}

internal fun validateLocation(
        id: String?,
        name: String?,
        address: String?,
        coordinates: Coordinates?
): Either<String, Location?> {
    coordinates ?: return null.right()
    return Location.of(id,
            name?.trim(),
            address?.trim(),
            coordinates
    ).right()
}

internal fun validateApproximateAppearance(
        ageRange: AgeRange?,
        heightRange: HeightRange?,
        gender: Gender?,
        skin: Skin?,
        hair: Hair?
): Either<String, ApproximateAppearance> {
    return ApproximateAppearance.of(
            gender,
            skin,
            hair,
            ageRange,
            heightRange
    ).mapLeft(ApproximateAppearance.Exception::localizedMessage)
}

internal fun validateExactAppearance(
        age: Age,
        height: Height,
        gender: Gender,
        skin: Skin,
        hair: Hair
): Either<String, ExactAppearance> {
    return ExactAppearance.of(
            gender,
            skin,
            hair,
            age,
            height
    ).right()
}

internal fun validateAppPublishedChild(
        user: SimpleRetrievedUser,
        name: Either<Name, FullName>?,
        notes: String?,
        location: Location?,
        appearance: ApproximateAppearance,
        picturePath: PicturePath?,
        imageLoader: ImageLoader
): Either<String, AppChildToPublish> {
    return AppChildToPublish.of(
            user,
            name,
            notes?.trim(),
            location,
            appearance,
            picturePath,
            imageLoader
    ).mapLeft(ChildToPublish.Exception::localizedMessage)
}

internal fun validateChildQuery(
        user: SimpleRetrievedUser,
        fullName: FullName,
        location: Location,
        appearance: ExactAppearance
): Either<String, ChildQuery> {
    return ChildQuery.of(
            user,
            fullName,
            location,
            appearance
    ).right()
}
