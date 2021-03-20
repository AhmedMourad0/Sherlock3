package dev.ahmedmourad.sherlock.children.remote.utils

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import dev.ahmedmourad.sherlock.children.remote.contract.Contract
import dev.ahmedmourad.sherlock.children.remote.model.QueryId
import dev.ahmedmourad.sherlock.children.remote.model.QueryResult
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.*
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import timber.log.Timber
import timber.log.error

fun ChildToPublish.toMap(pictureUrl: Url?): Map<String, Any?> = hashMapOf(
        Contract.Database.Children.USER_ID to user.id.value,
        Contract.Database.Children.TIMESTAMP to FieldValue.serverTimestamp(),
        Contract.Database.Children.LOCATION_ID to location?.id,
        Contract.Database.Children.LOCATION_NAME to location?.name,
        Contract.Database.Children.LOCATION_ADDRESS to location?.address,
        Contract.Database.Children.LOCATION_LATITUDE to location?.coordinates?.latitude,
        Contract.Database.Children.LOCATION_LONGITUDE to location?.coordinates?.longitude,
        Contract.Database.Children.MIN_AGE to appearance.ageRange?.min?.value,
        Contract.Database.Children.MAX_AGE to appearance.ageRange?.max?.value,
        Contract.Database.Children.MIN_HEIGHT to appearance.heightRange?.min?.value,
        Contract.Database.Children.MAX_HEIGHT to appearance.heightRange?.max?.value,
        Contract.Database.Children.GENDER to appearance.gender?.value,
        Contract.Database.Children.SKIN to appearance.skin?.value,
        Contract.Database.Children.HAIR to appearance.hair?.value,
        Contract.Database.Children.NOTES to notes,
        Contract.Database.Children.PICTURE_URL to pictureUrl?.value
) + createNameHashMap(name)

private fun createNameHashMap(name: Either<Name, FullName>?): Map<String, Any?> {

    if (name == null) {
        return hashMapOf(
                Contract.Database.Children.FIRST_NAME to null,
                Contract.Database.Children.LAST_NAME to null
        )
    }

    return name.fold(ifLeft = {
        hashMapOf(
                Contract.Database.Children.FIRST_NAME to it.value,
                Contract.Database.Children.LAST_NAME to null
        )
    }, ifRight = {
        hashMapOf(
                Contract.Database.Children.FIRST_NAME to it.first.value,
                Contract.Database.Children.LAST_NAME to it.last.value
        )
    })
}

fun ChildrenQuery.toMap(): Map<String, Any?> = hashMapOf(
        Contract.Database.Queries.PAGE to this.page,
        Contract.Database.Queries.TIMESTAMP to this.timestamp,
        Contract.Database.Queries.USER_ID to this.user.id.value,
        Contract.Database.Queries.FIRST_NAME to this.fullName.first.value,
        Contract.Database.Queries.LAST_NAME to this.fullName.last.value,
        Contract.Database.Queries.LATITUDE to this.coordinates.latitude,
        Contract.Database.Queries.LONGITUDE to this.coordinates.longitude,
        Contract.Database.Queries.GENDER to this.appearance.gender.value,
        Contract.Database.Queries.SKIN to this.appearance.skin.value,
        Contract.Database.Queries.HAIR to this.appearance.hair.value,
        Contract.Database.Queries.AGE to this.appearance.age.value,
        Contract.Database.Queries.HEIGHT to this.appearance.height.value
)

fun Investigation.toMap(): Map<String, Any?> = hashMapOf(
        Contract.Database.Investigations.TIMESTAMP to FieldValue.serverTimestamp(),
        Contract.Database.Investigations.USER_ID to this.user.id.value,
        Contract.Database.Investigations.FIRST_NAME to this.fullName.first.value,
        Contract.Database.Investigations.LAST_NAME to this.fullName.last.value,
        Contract.Database.Investigations.LATITUDE to this.coordinates.latitude,
        Contract.Database.Investigations.LONGITUDE to this.coordinates.longitude,
        Contract.Database.Investigations.GENDER to this.appearance.gender.value,
        Contract.Database.Investigations.SKIN to this.appearance.skin.value,
        Contract.Database.Investigations.HAIR to this.appearance.hair.value,
        Contract.Database.Investigations.AGE to this.appearance.age.value,
        Contract.Database.Investigations.HEIGHT to this.appearance.height.value
)

internal fun extractQueryResult(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, QueryResult> {

    val id = QueryId(snapshot.id)

    val childId = snapshot.getString(Contract.Database.Queries.Results.CHILD_ID)
            ?.let(::ChildId) ?: return ModelCreationException("childId is null for id=$id").left()

    return Either.fx {

        val weight = snapshot.getDouble(Contract.Database.Queries.Results.WEIGHT)
                ?.let(Weight.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?: ModelCreationException("weight is null for id=$id").left()

        QueryResult(
                id,
                childId,
                weight.bind()
        ).right().bind()
    }
}

internal fun extractChildInvestigation(
        user: SimpleRetrievedUser,
        snapshot: DocumentSnapshot
): Either<ModelCreationException, Investigation> {

    val id = snapshot.id

    val timestamp = snapshot.getTimestamp(Contract.Database.Investigations.TIMESTAMP)
            ?.seconds
            ?.let { it * 1000L }
            ?: return ModelCreationException("timestamp is null for id=$id").left()

    return Either.fx {

        val firstName = snapshot.getString(Contract.Database.Investigations.FIRST_NAME)
                ?.let(Name.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?: ModelCreationException("first name is null for id=$id").left()

        val lastName = snapshot.getString(Contract.Database.Investigations.LAST_NAME)
                ?.let(Name.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?: ModelCreationException("last name is null for id=$id").left()

        val fullName = FullName.of(firstName.bind(), lastName.bind())

        val coordinates = extractCoordinates(
                snapshot,
                Contract.Database.Investigations.LATITUDE,
                Contract.Database.Investigations.LONGITUDE
        ).bind()?.right() ?: ModelCreationException("last name is null for id=$id").left()

        val appearance = extractExactAppearance(snapshot, id).bind()

        Investigation.of(
                timestamp,
                user,
                fullName,
                coordinates.bind(),
                appearance
        )
    }
}

internal fun extractSimpleRetrievedChild(
        snapshot: DocumentSnapshot,
        user: SimpleRetrievedUser
): Either<ModelCreationException, SimpleRetrievedChild> {

    val id = snapshot.id

    val timestamp = snapshot.getTimestamp(Contract.Database.Children.TIMESTAMP)
            ?.seconds
            ?.let { it * 1000L }
            ?: return ModelCreationException("timestamp is null for id=$id").left()

    val pictureUrl = snapshot.getString(Contract.Database.Children.PICTURE_URL)
            ?.let(Url.Companion::of)
            ?.mapLeft { ModelCreationException(it.toString()) }
            ?.getOrHandle {
                Timber.error(it, it::toString)
                null
            }

    val name = extractName(snapshot).getOrHandle {
        Timber.error(it, it::toString)
        null
    }

    return SimpleRetrievedChild.of(
            ChildId(id),
            user,
            timestamp,
            name,
            snapshot.getString(Contract.Database.Children.NOTES),
            snapshot.getString(Contract.Database.Children.LOCATION_NAME),
            snapshot.getString(Contract.Database.Children.LOCATION_ADDRESS),
            pictureUrl
    ).mapLeft { ModelCreationException(it.toString()) }
}

internal fun extractRetrievedChild(
        snapshot: DocumentSnapshot,
        user: SimpleRetrievedUser
): Either<ModelCreationException, RetrievedChild> {

    val id = snapshot.id

    val timestamp = snapshot.getTimestamp(Contract.Database.Children.TIMESTAMP)
            ?.seconds
            ?.let { it * 1000L }
            ?: return ModelCreationException("timestamp is null for id=$id").left()

    val pictureUrl = snapshot.getString(Contract.Database.Children.PICTURE_URL)
            ?.let(Url.Companion::of)
            ?.mapLeft { ModelCreationException(it.toString()) }
            ?.getOrHandle {
                Timber.error(it, it::toString)
                null
            }

    val name = extractName(snapshot).getOrHandle {
        Timber.error(it, it::toString)
        null
    }

    val location = extractLocation(snapshot).getOrHandle {
        Timber.error(it, it::toString)
        null
    }

    return Either.fx {

        val appearance = !extractApproximateAppearance(snapshot)

        RetrievedChild.of(
                ChildId(id),
                user,
                timestamp,
                name,
                snapshot.getString(Contract.Database.Children.NOTES),
                location,
                appearance,
                pictureUrl
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractName(
        snapshot: DocumentSnapshot,
): Either<ModelCreationException, Either<Name, FullName>?> {
    return Either.fx {

        val first = snapshot.getString(Contract.Database.Children.FIRST_NAME) ?: return@fx null

        val firstName = !Name.of(first).mapLeft { ModelCreationException(it.toString()) }

        val last = snapshot.getString(Contract.Database.Children.LAST_NAME)
                ?: return@fx firstName.left()

        val lastName = !Name.of(last).mapLeft { ModelCreationException(it.toString()) }

        FullName.of(firstName, lastName).right().right().bind()
    }
}

private fun extractApproximateAppearance(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, ApproximateAppearance> {
    return Either.fx {

        val gender = snapshot.getLong(Contract.Database.Children.GENDER)?.toInt()
                ?.let { findEnum(it, Gender.values()) }

        val skin = snapshot.getLong(Contract.Database.Children.SKIN)?.toInt()
                ?.let { findEnum(it, Skin.values()) }

        val hair = snapshot.getLong(Contract.Database.Children.HAIR)?.toInt()
                ?.let { findEnum(it, Hair.values()) }

        val ageRange = !extractAgeRange(snapshot).mapLeft { ModelCreationException(it.toString()) }

        val heightRange = !extractHeightRange(snapshot).mapLeft { ModelCreationException(it.toString()) }

        ApproximateAppearance.of(
                gender,
                skin,
                hair,
                ageRange,
                heightRange
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractAgeRange(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, AgeRange?> {
    return Either.fx {

        val min = snapshot.getLong(Contract.Database.Children.MIN_AGE)?.toInt() ?: return@fx null

        val minAge = !Age.of(min).mapLeft { ModelCreationException(it.toString()) }

        val max = snapshot.getLong(Contract.Database.Children.MAX_AGE)?.toInt() ?: return@fx null

        val maxAge = !Age.of(max).mapLeft { ModelCreationException(it.toString()) }

        AgeRange.of(minAge, maxAge).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractHeightRange(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, HeightRange?> {
    return Either.fx {

        val min = snapshot.getLong(Contract.Database.Children.MIN_HEIGHT)?.toInt() ?: return@fx null

        val minHeight = !Height.of(min).mapLeft { ModelCreationException(it.toString()) }

        val max = snapshot.getLong(Contract.Database.Children.MAX_HEIGHT)?.toInt() ?: return@fx null

        val maxHeight = !Height.of(max).mapLeft { ModelCreationException(it.toString()) }

        HeightRange.of(minHeight, maxHeight).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}

private fun extractLocation(
        snapshot: DocumentSnapshot
): Either<ModelCreationException, Location?> {
    return Either.fx {

        val locationId = snapshot.getString(Contract.Database.Children.LOCATION_ID)
        val locationName = snapshot.getString(Contract.Database.Children.LOCATION_NAME)
        val locationAddress = snapshot.getString(Contract.Database.Children.LOCATION_ADDRESS)

        val coordinates = extractCoordinates(
                snapshot,
                Contract.Database.Children.LOCATION_LATITUDE,
                Contract.Database.Children.LOCATION_LONGITUDE
        ).bind() ?: return@fx null

        Location.of(
                locationId,
                locationName,
                locationAddress,
                coordinates
        ).right().bind()
    }
}

internal fun extractCoordinates(
        snapshot: DocumentSnapshot,
        latitudeKey: String,
        longitudeKey: String
): Either<ModelCreationException, Coordinates?> {

    val latitude = snapshot.getDouble(latitudeKey)
            ?: return null.right()
    val longitude = snapshot.getDouble(longitudeKey)
            ?: return null.right()

    return Coordinates.of(latitude, longitude).mapLeft { ModelCreationException(it.toString()) }
}

internal fun extractExactAppearance(
        snapshot: DocumentSnapshot,
        id: String
): Either<ModelCreationException, ExactAppearance> {
    return Either.fx {

        val gender = snapshot.getLong(Contract.Database.Investigations.GENDER)
                ?.toInt()
                ?.let { findEnum(it, Gender.values()) }
                ?.right()
                ?: ModelCreationException("gender is null for id=$id").left()

        val skin = snapshot.getLong(Contract.Database.Investigations.SKIN)
                ?.toInt()
                ?.let { findEnum(it, Skin.values()) }
                ?.right()
                ?: ModelCreationException("skin is null for id=$id").left()

        val hair = snapshot.getLong(Contract.Database.Investigations.HAIR)
                ?.toInt()
                ?.let { findEnum(it, Hair.values()) }
                ?.right()
                ?: ModelCreationException("hair is null for id=$id").left()

        val age = snapshot.getLong(Contract.Database.Investigations.AGE)
                ?.toInt()
                ?.let(Age.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?: ModelCreationException("age is null for id=$id").left()

        val height = snapshot.getLong(Contract.Database.Investigations.HEIGHT)
                ?.toInt()
                ?.let(Height.Companion::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?: ModelCreationException("height is null for id=$id").left()

        ExactAppearance.of(
                gender.bind(),
                skin.bind(),
                hair.bind(),
                age.bind(),
                height.bind()
        ).right().bind()
    }
}
