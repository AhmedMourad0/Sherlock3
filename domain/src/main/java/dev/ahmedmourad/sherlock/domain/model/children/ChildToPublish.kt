package dev.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.EitherSerializer
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
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
@Serializable
@NoCopy
data class ChildToPublish private constructor(
        val user: SimpleRetrievedUser,
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val location: Location?,
        val appearance: ApproximateAppearance,
        val picture: ByteArray?
) {

    fun toRetrievedChild(
            id: ChildId,
            timestamp: Long,
            pictureUrl: Url?
    ): RetrievedChild {
        return RetrievedChild.of(
                id,
                user,
                timestamp,
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChildToPublish

        if (user != other.user) return false
        if (name != other.name) return false
        if (notes != other.notes) return false
        if (location != other.location) return false
        if (appearance != other.appearance) return false
        if (picture != null) {
            if (other.picture == null) return false
            if (!picture.contentEquals(other.picture)) return false
        } else if (other.picture != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + appearance.hashCode()
        result = 31 * result + (picture?.contentHashCode() ?: 0)
        return result
    }

    companion object {

        fun of(user: SimpleRetrievedUser,
               name: Either<Name, FullName>?,
               notes: String?,
               location: Location?,
               appearance: ApproximateAppearance,
               picture: ByteArray?
        ): Either<Exception, ChildToPublish> {
            return validate(user, name, notes, location, appearance, picture)?.left()
                    ?: ChildToPublish(user, name, notes, location, appearance, picture).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(user: SimpleRetrievedUser,
                     name: Either<Name, FullName>?,
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
