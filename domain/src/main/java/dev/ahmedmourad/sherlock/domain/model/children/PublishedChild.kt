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

//TODO: make the rules a little stricter
//TODO: add finding date
//TODO: add user id
@Serializable
class PublishedChild private constructor(
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val location: Location?,
        val appearance: ApproximateAppearance,
        val picture: ByteArray?
) {

    fun toRetrievedChild(id: ChildId, publicationDate: Long, pictureUrl: Url?): RetrievedChild {
        return RetrievedChild.of(
                id,
                publicationDate,
                name,
                notes,
                location,
                appearance,
                pictureUrl
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }

    fun component1() = name

    fun component2() = notes

    fun component3() = location

    fun component4() = appearance

    fun component5() = picture

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as PublishedChild

        if (name != other.name)
            return false

        if (notes != other.notes)
            return false

        if (location != other.location)
            return false

        if (appearance != other.appearance)
            return false

        if (picture != null) {
            if (other.picture == null) {
                return false
            }
            if (!picture.contentEquals(other.picture)) {
                return false
            }
        } else if (other.picture != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + appearance.hashCode()
        result = 31 * result + (picture?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "PublishedChild(" +
                "name=$name, " +
                "notes=$notes, " +
                "location=$location, " +
                "appearance=$appearance, " +
                "picture=${picture?.contentToString()}" +
                ")"
    }

    companion object {

        fun of(name: Either<Name, FullName>?,
               notes: String?,
               location: Location?,
               appearance: ApproximateAppearance,
               picture: ByteArray?
        ): Either<Exception, PublishedChild> {
            return validate(name, notes, location, appearance, picture)?.left()
                    ?: PublishedChild(name, notes, location, appearance, picture).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(name: Either<Name, FullName>?,
                     notes: String?,
                     location: Location?,
                     appearance: ApproximateAppearance,
                     picture: ByteArray?
        ): Exception? {
            return if (name != null || notes != null || location != null || picture != null) {
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
