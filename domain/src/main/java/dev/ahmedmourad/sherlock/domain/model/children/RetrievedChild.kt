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

@Serializable
@NoCopy
data class RetrievedChild private constructor(
        val id: ChildId,
        val user: SimpleRetrievedUser,
        val timestamp: Long,
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val location: Location?,
        val appearance: ApproximateAppearance,
        val pictureUrl: Url?
) {

    fun simplify(): SimpleRetrievedChild {
        return SimpleRetrievedChild.of(
                id,
                user,
                timestamp,
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

    companion object {

        fun of(id: ChildId,
               user: SimpleRetrievedUser,
               timestamp: Long,
               name: Either<Name, FullName>?,
               notes: String?,
               location: Location?,
               appearance: ApproximateAppearance,
               pictureUrl: Url?
        ): Either<Exception, RetrievedChild> {
            return validate(id, user, timestamp, name, notes, location, appearance, pictureUrl)?.left()
                    ?: RetrievedChild(id, user, timestamp, name, notes, location, appearance, pictureUrl).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: ChildId,
                     user: SimpleRetrievedUser,
                     timestamp: Long,
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
