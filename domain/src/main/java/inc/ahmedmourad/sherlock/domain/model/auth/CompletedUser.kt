package inc.ahmedmourad.sherlock.domain.model.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
class CompletedUser private constructor(
        val id: UserId,
        val email: Email,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picture: ByteArray?
) {

    fun component1() = id

    fun component2() = email

    fun component3() = displayName

    fun component4() = phoneNumber

    fun component5() = picture

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as CompletedUser

        if (id != other.id)
            return false

        if (email != other.email)
            return false

        if (displayName != other.displayName)
            return false

        if (phoneNumber != other.phoneNumber)
            return false

        if (picture != null) {

            if (other.picture == null)
                return false

            if (!picture.contentEquals(other.picture))
                return false

        } else if (other.picture != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + (picture?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CompletedUser(" +
                "id=$id, " +
                "email=$email, " +
                "displayName=$displayName, " +
                "phoneNumber=$phoneNumber, " +
                "picture=${picture?.contentToString()}" +
                ")"
    }

    companion object {

        fun of(id: UserId,
               email: Email,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picture: ByteArray?
        ): Either<Exception, CompletedUser> {
            return validate(id, email, displayName, phoneNumber, picture)?.left()
                    ?: CompletedUser(id, email, displayName, phoneNumber, picture).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: UserId,
                     email: Email,
                     displayName: DisplayName,
                     phoneNumber: PhoneNumber,
                     picture: ByteArray?
        ): Exception? {
            return null
        }
    }

    sealed class Exception
}
