package dev.ahmedmourad.sherlock.children.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import arrow.core.*
import arrow.core.extensions.fx
import dev.ahmedmourad.sherlock.children.local.contract.Contract.ChildrenEntry
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.constants.findEnum
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.children.RetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.SimpleRetrievedChild
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId

@Entity(tableName = ChildrenEntry.TABLE_NAME)
internal data class RoomChildEntity(

        @PrimaryKey
        @ColumnInfo(name = ChildrenEntry.COLUMN_ID)
        val id: String,

        @ColumnInfo(name = ChildrenEntry.COLUMN_PUBLICATION_DATE)
        val publicationDate: Long,

        @ColumnInfo(name = ChildrenEntry.COLUMN_FIRST_NAME)
        val firstName: String?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_LAST_NAME)
        val lastName: String?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_LOCATION_ID)
        val locationId: String?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_LOCATION_NAME)
        val locationName: String?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_LOCATION_ADDRESS)
        val locationAddress: String?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_LOCATION_LATITUDE)
        val locationLatitude: Double?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_LOCATION_LONGITUDE)
        val locationLongitude: Double?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_NOTES)
        val notes: String?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_GENDER)
        val gender: Int?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_SKIN)
        val skin: Int?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_HAIR)
        val hair: Int?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_MIN_AGE)
        val minAge: Int?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_MAX_AGE)
        val maxAge: Int?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_MIN_HEIGHT)
        val minHeight: Int?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_MAX_HEIGHT)
        val maxHeight: Int?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_PICTURE_URL)
        val pictureUrl: String?,

        @ColumnInfo(name = ChildrenEntry.COLUMN_WEIGHT)
        val weight: Double?
) {

    fun toRetrievedChild(): Either<ModelConversionException, Tuple2<RetrievedChild, Weight?>> {

        return Either.fx {

            val name = extractName().bind()

            val location = extractLocation().bind()

            val url = pictureUrl?.let(Url.Companion::of)
                    ?.mapLeft { ModelConversionException(it.toString()) }
                    ?.bind()

            val weight = this@RoomChildEntity.weight?.let(Weight.Companion::of)
                    ?.mapLeft { ModelConversionException(it.toString()) }
                    ?.bind()

            val (appearance) = extractApproximateAppearance()

            RetrievedChild.of(
                    ChildId(id),
                    publicationDate,
                    name,
                    notes,
                    location,
                    appearance,
                    url
            ).bimap(
                    leftOperation = { ModelConversionException(it.toString()) },
                    rightOperation = { it toT weight }
            ).bind()
        }
    }

    fun simplify(): Either<ModelConversionException, Tuple2<SimpleRetrievedChild, Weight?>?> {
        return Either.fx {

            val name = extractName().bind()

            val url = pictureUrl?.let(Url.Companion::of)
                    ?.mapLeft { ModelConversionException(it.toString()) }
                    ?.bind()

            val weight = this@RoomChildEntity.weight?.let(Weight.Companion::of)
                    ?.mapLeft { ModelConversionException(it.toString()) }
                    ?.bind()

            SimpleRetrievedChild.of(
                    ChildId(id),
                    publicationDate,
                    name,
                    notes,
                    locationName,
                    locationAddress,
                    url
            ).bimap(
                    leftOperation = { ModelConversionException(it.toString()) },
                    rightOperation = { it toT weight }
            ).bind()
        }
    }

    private fun extractName(): Either<ModelConversionException, Either<Name, FullName>?> {
        return Either.fx {

            firstName ?: return@fx null

            val (first) = Name.of(firstName).mapLeft { ModelConversionException(it.toString()) }

            lastName ?: return@fx first.left()

            val (last) = Name.of(lastName).mapLeft { ModelConversionException(it.toString()) }

            FullName.of(first, last).right().right().bind()
        }
    }

    private fun extractApproximateAppearance(): Either<ModelConversionException, ApproximateAppearance> {
        return Either.fx {

            val gender = gender?.let { findEnum(it, Gender.values()) }
            val skin = skin?.let { findEnum(it, Skin.values()) }
            val hair = hair?.let { findEnum(it, Hair.values()) }

            val ageRange = extractAgeRange().bind()

            val heightRange = extractHeightRange().bind()

            ApproximateAppearance.of(
                    gender,
                    skin,
                    hair,
                    ageRange,
                    heightRange
            ).mapLeft { ModelConversionException(it.toString()) }.bind()
        }
    }

    private fun extractAgeRange(): Either<ModelConversionException, AgeRange?> {
        return Either.fx {

            minAge ?: return@fx null
            maxAge ?: return@fx null

            val (min) = Age.of(minAge).mapLeft { ModelConversionException(it.toString()) }
            val (max) = Age.of(maxAge).mapLeft { ModelConversionException(it.toString()) }

            AgeRange.of(min, max).mapLeft { ModelConversionException(it.toString()) }.bind()
        }
    }

    private fun extractHeightRange(): Either<ModelConversionException, HeightRange?> {
        return Either.fx {

            minHeight ?: return@fx null
            maxHeight ?: return@fx null

            val (min) = Height.of(minHeight).mapLeft { ModelConversionException(it.toString()) }
            val (max) = Height.of(maxHeight).mapLeft { ModelConversionException(it.toString()) }

            HeightRange.of(min, max).mapLeft { ModelConversionException(it.toString()) }.bind()
        }
    }

    private fun extractLocation(): Either<ModelConversionException, Location?> {
        return Either.fx {

            val id = locationId ?: return@fx null
            val name = locationName ?: return@fx null
            val address = locationAddress ?: return@fx null

            val coordinates = extractCoordinates().bind() ?: return@fx null

            Location.of(
                    id,
                    name,
                    address,
                    coordinates
            ).right().bind()
        }
    }

    private fun extractCoordinates(): Either<ModelConversionException, Coordinates?> {
        return Either.fx {

            val latitude = locationLatitude ?: return@fx null
            val longitude = locationLongitude ?: return@fx null

            Coordinates.of(latitude, longitude)
                    .mapLeft { ModelConversionException(it.toString()) }.bind()
        }
    }
}
