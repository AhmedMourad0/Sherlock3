package dev.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.EitherSerializer
import dev.ahmedmourad.sherlock.domain.model.children.submodel.ApproximateAppearance
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Location
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import kotlinx.serialization.Serializable
import timber.log.Timber
import timber.log.error

//TODO: add user id
@Serializable
class RetrievedChild private constructor(
        val id: ChildId,
        val publicationDate: Long,
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val location: Location?,
        val appearance: ApproximateAppearance,
        val pictureUrl: Url?
) {

    fun simplify(): SimpleRetrievedChild {
        return SimpleRetrievedChild.of(
                id,
                publicationDate,
                name,
                notes,
                location?.name,
                location?.address,
                pictureUrl
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }

    fun component1() = id

    fun component2() = publicationDate

    fun component3() = name

    fun component4() = notes

    fun component5() = location

    fun component6() = appearance

    fun component7() = pictureUrl

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as RetrievedChild

        if (id != other.id)
            return false

        if (publicationDate != other.publicationDate)
            return false

        if (name != other.name)
            return false

        if (notes != other.notes)
            return false

        if (location != other.location)
            return false

        if (appearance != other.appearance)
            return false

        if (pictureUrl != other.pictureUrl)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + publicationDate.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + appearance.hashCode()
        result = 31 * result + pictureUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return "RetrievedChild(" +
                "id='$id', " +
                "publicationDate=$publicationDate, " +
                "name=$name, " +
                "notes=$notes, " +
                "location=$location, " +
                "appearance=$appearance, " +
                "pictureUrl=$pictureUrl" +
                ")"
    }

    companion object {

        fun of(id: ChildId,
               publicationDate: Long,
               name: Either<Name, FullName>?,
               notes: String?,
               location: Location?,
               appearance: ApproximateAppearance,
               pictureUrl: Url?
        ): Either<Exception, RetrievedChild> {
            return validate(id, publicationDate, name, notes, location, appearance, pictureUrl)?.left()
                    ?: RetrievedChild(id, publicationDate, name, notes, location, appearance, pictureUrl).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: ChildId,
                     publicationDate: Long,
                     name: Either<Name, FullName>?,
                     notes: String?,
                     location: Location?,
                     appearance: ApproximateAppearance,
                     pictureUrl: Url?
        ): Exception? {
            return if (name != null || notes != null || location != null || pictureUrl != null) {
                null
            } else {
                Exception.NotEnoughDetailsException
            }
        }
    }

    sealed class Exception {
        object NotEnoughDetailsException : Exception()
    }
}
