package dev.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.EitherSerializer
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId
import kotlinx.serialization.Serializable

//TODO: follow PublishedChild's rules
//TODO: add user id
@Serializable
@NoCopy
data class SimpleRetrievedChild private constructor(
        val id: ChildId,
        val publicationDate: Long,
        val name: @Serializable(with = EitherSerializer::class) Either<Name, FullName>?,
        val notes: String?,
        val locationName: String?,
        val locationAddress: String?,
        val pictureUrl: Url?
) {

    companion object {

        fun of(id: ChildId,
               publicationDate: Long,
               name: Either<Name, FullName>?,
               notes: String?,
               locationName: String?,
               locationAddress: String?,
               pictureUrl: Url?
        ): Either<Exception, SimpleRetrievedChild> {
            return validate(id, publicationDate, name, notes, locationName, locationAddress, pictureUrl)?.left()
                    ?: SimpleRetrievedChild(id, publicationDate, name, notes, locationName, locationAddress, pictureUrl).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: ChildId,
                     publicationDate: Long,
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
