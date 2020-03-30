package inc.ahmedmourad.sherlock.viewmodel.controllers.validators.children

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.model.children.ChildQuery
import inc.ahmedmourad.sherlock.domain.model.children.PublishedChild
import inc.ahmedmourad.sherlock.domain.model.children.submodel.*
import inc.ahmedmourad.sherlock.domain.model.common.Name
import inc.ahmedmourad.sherlock.domain.model.common.PicturePath
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import inc.ahmedmourad.sherlock.model.localizedMessage
import splitties.init.appCtx

internal fun validateFullName(
        firstName: Name,
        lastName: Name
): Either<String, FullName> {
    return FullName.of(firstName, lastName).mapLeft(FullName.Exception::localizedMessage)
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

internal fun validateGender(gender: Gender?): Either<String, Gender> {
    return gender?.right() ?: appCtx.getString(R.string.gender_missing).left()
}

internal fun validateSkin(skin: Skin?): Either<String, Skin> {
    return skin?.right() ?: appCtx.getString(R.string.skin_color_missing).left()
}

internal fun validateHair(hair: Hair?): Either<String, Hair> {
    return hair?.right() ?: appCtx.getString(R.string.hair_color_missing).left()
}

internal fun validateCoordinates(latitude: Double, longitude: Double): Either<String, Coordinates> {
    return Coordinates.of(latitude, longitude).mapLeft(Coordinates.Exception::localizedMessage)
}

internal fun validateLocation(location: Location?): Either<String, Location> {
    return location?.right() ?: appCtx.getString(R.string.invalid_last_known_location).left()
}

internal fun validateLocation(
        id: String,
        name: String,
        address: String,
        coordinates: Coordinates
): Either<String, Location> {
    return Location.of(id,
            name.trim(),
            address.trim(),
            coordinates
    ).mapLeft(Location.Exception::localizedMessage)
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
    ).mapLeft(ExactAppearance.Exception::localizedMessage)
}

internal fun validateAppPublishedChild(
        name: Either<Name, FullName>?,
        notes: String?,
        location: Location?,
        appearance: ApproximateAppearance,
        picturePath: PicturePath?
): Either<String, AppPublishedChild> {
    return AppPublishedChild.of(
            name,
            notes?.trim(),
            location,
            appearance,
            picturePath
    ).mapLeft(PublishedChild.Exception::localizedMessage)
}

internal fun validateChildQuery(
        fullName: FullName,
        location: Location,
        appearance: ExactAppearance
): Either<String, ChildQuery> {
    return ChildQuery.of(
            fullName,
            location,
            appearance
    ).mapLeft(ChildQuery.Exception::localizedMessage)
}
