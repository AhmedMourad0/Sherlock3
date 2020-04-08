package inc.ahmedmourad.sherlock.domain.model.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import inc.ahmedmourad.sherlock.domain.model.common.Url
import inc.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
class User private constructor(
        val id: UserId,
        val registrationDate: Long,
        val lastLoginDate: Long,
        val email: Email,
        val displayName: DisplayName,
        val username: Username,
        val phoneNumber: PhoneNumber,
        val pictureUrl: Url?
) {

    fun component1() = id

    fun component2() = registrationDate

    fun component3() = lastLoginDate

    fun component4() = email

    fun component5() = displayName

    fun component6() = username

    fun component7() = phoneNumber

    fun component8() = pictureUrl

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as User

        if (id != other.id)
            return false

        if (registrationDate != other.registrationDate)
            return false

        if (lastLoginDate != other.lastLoginDate)
            return false

        if (email != other.email)
            return false

        if (displayName != other.displayName)
            return false

        if (username != other.username)
            return false

        if (phoneNumber != other.phoneNumber)
            return false

        if (pictureUrl != other.pictureUrl)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + registrationDate.hashCode()
        result = 31 * result + lastLoginDate.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + pictureUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(" +
                "id=$id, " +
                "registrationDate=$registrationDate, " +
                "lastLoginDate=$lastLoginDate, " +
                "email=$email, " +
                "displayName=$displayName, " +
                "userName=$username, " +
                "phoneNumber=$phoneNumber, " +
                "pictureUrl=$pictureUrl" +
                ")"
    }

    companion object {

        fun of(id: UserId,
               registrationDate: Long,
               lastLoginDate: Long,
               email: Email,
               displayName: DisplayName,
               username: Username,
               phoneNumber: PhoneNumber,
               pictureUrl: Url?
        ): Either<Exception, User> {
            return validate(id, registrationDate, lastLoginDate, email, displayName, username, phoneNumber, pictureUrl)?.left()
                    ?: User(id, registrationDate, lastLoginDate, email, displayName, username, phoneNumber, pictureUrl).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: UserId,
                     registrationDate: Long,
                     lastLoginDate: Long,
                     email: Email,
                     displayName: DisplayName,
                     username: Username,
                     phoneNumber: PhoneNumber,
                     pictureUrl: Url?
        ): Exception? {
            return null
        }
    }

    sealed class Exception
}
