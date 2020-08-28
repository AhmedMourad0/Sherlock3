package dev.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.EitherSerializer
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class SimpleRetrievedChild private constructor(
        val id: ChildId,
        val user: SimpleRetrievedUser,
        val timestamp: Long,
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val locationName: String?,
        val locationAddress: String?,
        val pictureUrl: Url?
) {

    companion object {

        fun of(id: ChildId,
               user: SimpleRetrievedUser,
               timestamp: Long,
               name: Either<Name, FullName>?,
               notes: String?,
               locationName: String?,
               locationAddress: String?,
               pictureUrl: Url?
        ): Either<Exception, SimpleRetrievedChild> {
            return validate(id, user, timestamp, name, notes, locationName, locationAddress, pictureUrl)?.left()
                    ?: SimpleRetrievedChild(id, user, timestamp, name, notes, locationName, locationAddress, pictureUrl).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: ChildId,
                     user: SimpleRetrievedUser,
                     timestamp: Long,
                     name: Either<Name, FullName>?,
                     notes: String?,
                     locationName: String?,
                     locationAddress: String?,
                     pictureUrl: Url?
        ): Exception? {
            return if (name != null || notes != null || locationName != null || locationAddress != null || pictureUrl != null) {
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
