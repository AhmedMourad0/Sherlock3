package dev.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
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
@NoCopy
data class PublishedChild private constructor(
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
