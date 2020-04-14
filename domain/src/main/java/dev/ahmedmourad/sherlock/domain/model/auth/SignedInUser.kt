package dev.ahmedmourad.sherlock.domain.model.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Username
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
class SignedInUser private constructor(
        val id: UserId,
        val registrationDate: Long,
        val email: Email,
        val displayName: DisplayName,
        val username: Username,
        val phoneNumber: PhoneNumber,
        val pictureUrl: Url?
) {

    fun component1() = id

    fun component2() = registrationDate

    fun component3() = email

    fun component4() = displayName

    fun component5() = username

    fun component6() = phoneNumber

    fun component7() = pictureUrl

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as SignedInUser

        if (id != other.id)
            return false

        if (registrationDate != other.registrationDate)
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
        result = 31 * result + email.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + pictureUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return "SignedInUser(" +
                "id=$id, " +
                "registrationDate=$registrationDate, " +
                "email=$email, " +
                "displayName=$displayName, " +
                "username=$username, " +
                "phoneNumber=$phoneNumber, " +
                "pictureUrl=$pictureUrl" +
                ")"
    }

    companion object {

        fun of(id: UserId,
               registrationDate: Long,
               email: Email,
               displayName: DisplayName,
               username: Username,
               phoneNumber: PhoneNumber,
               pictureUrl: Url?
        ): Either<Exception, SignedInUser> {
            return validate(id, registrationDate, email, displayName, username, phoneNumber, pictureUrl)?.left()
                    ?: SignedInUser(id, registrationDate, email, displayName, username, phoneNumber, pictureUrl).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: UserId,
                     registrationDate: Long,
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
