package dev.ahmedmourad.sherlock.children.local.utils

import arrow.core.*
import arrow.core.extensions.fx
import dev.ahmedmourad.sherlock.children.local.FindAllSimpleWhereWeightExists
import dev.ahmedmourad.sherlock.children.local.FindChildById
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import dev.ahmedmourad.sherlock.domain.model.ids.UserId

fun FindChildById.map(
        childId: ChildId
): Either<ModelCreationException, Tuple2<RetrievedChild, Weight?>> {
    return Either.fx {

        val uDisplayName = DisplayName.of(display_name)
                .mapLeft { ModelCreationException(it.toString()) }
                .bind()

        val uPictureUrl = picture_url_?.let(Url::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val user = SimpleRetrievedUser.of(
                id = UserId(id),
                displayName = uDisplayName,
                pictureUrl = uPictureUrl
        ).right().bind()

        val cName = parseName(first_name, last_name).bind()

        val cPictureUrl = picture_url?.let(Url::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val cLocation = parseLocation(
                location_id,
                location_name,
                location_address,
                location_latitude,
                location_longitude
        ).bind()

        val cAppearance = parseAppearance(
                gender?.toInt(),
                skin?.toInt(),
                hair?.toInt(),
                min_age?.toInt(),
                max_age?.toInt(),
                min_height?.toInt(),
                max_height?.toInt()
        ).bind()

        val cWeight = weight?.let(Weight::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        RetrievedChild.of(
                id = childId,
                user = user,
                timestamp = timestamp,
                name = cName,
                notes = notes,
                location = cLocation,
                appearance = cAppearance,
                pictureUrl = cPictureUrl
        ).bimap(leftOperation = {
            ModelCreationException(it.toString())
        }, rightOperation = {
            it toT cWeight
        }).bind()
    }
}

fun FindAllSimpleWhereWeightExists.map():
        Either<ModelCreationException, Pair<SimpleRetrievedChild, Weight>> {
    return Either.fx {

        val uDisplayName = DisplayName.of(display_name)
                .mapLeft { ModelCreationException(it.toString()) }
                .bind()

        val uPictureUrl = picture_url_?.let(Url::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val user = SimpleRetrievedUser.of(
                id = UserId(id_),
                displayName = uDisplayName,
                pictureUrl = uPictureUrl
        ).right().bind()

        val cName = parseName(first_name, last_name).bind()

        val cPictureUrl = picture_url?.let(Url::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val cWeight = Weight.of(weight!!)
                .mapLeft { ModelCreationException(it.toString()) }
                .bind()

        SimpleRetrievedChild.of(
                id = ChildId(id),
                user = user,
                timestamp = timestamp,
                name = cName,
                notes = notes,
                locationName = location_name,
                locationAddress = location_address,
                pictureUrl = cPictureUrl
        ).bimap(leftOperation = {
            ModelCreationException(it.toString())
        }, rightOperation = {
            it to cWeight
        }).bind()
    }
}

private fun parseName(
        first: String?,
        last: String?
): Either<ModelCreationException, Either<Name, FullName>?> {
    return Either.fx {
        when {

            first == null -> null

            last == null -> {
                Name.of(first)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()
                        .left()
            }

            else -> {

                val f = Name.of(first)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()

                val l = Name.of(last)
                        .mapLeft { ModelCreationException(it.toString()) }
                        .bind()

                FullName.of(f, l).right()
            }
        }
    }
}

private fun parseLocation(
        id: String?,
        name: String?,
        address: String?,
        latitude: Double?,
        longitude: Double?
): Either<ModelCreationException, Location?> {
    return Either.fx {

        latitude ?: return@fx null.right().bind()
        longitude ?: return@fx null.right().bind()

        val coordinates = Coordinates.of(latitude, longitude)
                .mapLeft { ModelCreationException(it.toString()) }
                .bind()

        Location.of(
                id,
                name,
                address,
                coordinates
        ).right().bind()
    }
}

private fun parseAppearance(
        gender: Int?,
        skin: Int?,
        hair: Int?,
        minAge: Int?,
        maxAge: Int?,
        minHeight: Int?,
        maxHeight: Int?
): Either<ModelCreationException, ApproximateAppearance> {
    return Either.fx {

        val g = gender?.let { findEnum(it, Gender.values()) }
        val s = skin?.let { findEnum(it, Skin.values()) }
        val h = hair?.let { findEnum(it, Hair.values()) }

        val minAgeWrapped = minAge?.let(Age::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val maxAgeWrapped = maxAge?.let(Age::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val age = if (minAgeWrapped == null || maxAgeWrapped == null) {
            null
        } else {
            AgeRange.of(minAgeWrapped, maxAgeWrapped)
                    .mapLeft { ModelCreationException(it.toString()) }
                    .bind()
        }

        val minHeightWrapped = minHeight?.let(Height::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val maxHeightWrapped = maxHeight?.let(Height::of)
                ?.mapLeft { ModelCreationException(it.toString()) }
                ?.bind()

        val height = if (minHeightWrapped == null || maxHeightWrapped == null) {
            null
        } else {
            HeightRange.of(minHeightWrapped, maxHeightWrapped)
                    .mapLeft { ModelCreationException(it.toString()) }
                    .bind()
        }

        ApproximateAppearance.of(
                g,
                s,
                h,
                age,
                height
        ).mapLeft { ModelCreationException(it.toString()) }.bind()
    }
}
