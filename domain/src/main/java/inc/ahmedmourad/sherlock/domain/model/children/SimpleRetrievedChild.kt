package inc.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import inc.ahmedmourad.sherlock.domain.model.common.Name
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.ChildId

//TODO: follow PublishedChild's rules
//TODO: add user id
class SimpleRetrievedChild private constructor(
        val id: ChildId,
        val publicationDate: Long,
        val name: Either<Name, FullName>?,
        val notes: String?,
        val locationName: String?,
        val locationAddress: String?,
        val pictureUrl: Url?
) {

    fun component1() = id

    fun component2() = publicationDate

    fun component3() = name

    fun component4() = notes

    fun component5() = locationName

    fun component6() = locationAddress

    fun component7() = pictureUrl

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as SimpleRetrievedChild

        if (id != other.id)
            return false

        if (publicationDate != other.publicationDate)
            return false

        if (name != other.name)
            return false

        if (notes != other.notes)
            return false

        if (locationName != other.locationName)
            return false

        if (locationAddress != other.locationAddress)
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
        result = 31 * result + locationName.hashCode()
        result = 31 * result + locationAddress.hashCode()
        result = 31 * result + pictureUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return "SimpleRetrievedChild(" +
                "id='$id'," +
                "publicationDate=$publicationDate, " +
                "name=$name, " +
                "notes=$notes, " +
                "locationName=$locationName, " +
                "locationAddress=$locationAddress, " +
                "pictureUrl=$pictureUrl" +
                ")"
    }

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
