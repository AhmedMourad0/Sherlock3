package dev.ahmedmourad.sherlock.domain.model.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.common.Url
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
class IncompleteUser private constructor(
        val id: UserId,
        val email: Email?,
        val displayName: DisplayName?,
        val phoneNumber: PhoneNumber?,
        val pictureUrl: Url?
) {

    fun component1() = id

    fun component2() = email

    fun component3() = displayName

    fun component4() = phoneNumber

    fun component5() = pictureUrl

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as IncompleteUser

        if (id != other.id)
            return false

        if (email != other.email)
            return false

        if (displayName != other.displayName)
            return false

        if (phoneNumber != other.phoneNumber)
            return false

        if (pictureUrl != other.pictureUrl)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + pictureUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return "IncompleteUser(" +
                "id=$id, " +
                "email=$email, " +
                "displayName=$displayName, " +
                "phoneNumber=$phoneNumber, " +
                "pictureUrl=$pictureUrl" +
                ")"
    }

    companion object {

        fun of(id: UserId,
               email: Email?,
               displayName: DisplayName?,
               phoneNumber: PhoneNumber?,
               pictureUrl: Url?
        ): Either<Exception, IncompleteUser> {
            return validate(id, email, displayName, phoneNumber, pictureUrl)?.left()
                    ?: IncompleteUser(id, email, displayName, phoneNumber, pictureUrl).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: UserId,
                     email: Email?,
                     displayName: DisplayName?,
                     phoneNumber: PhoneNumber?,
                     pictureUrl: Url?
        ): Exception? {
            return null
        }
    }

    sealed class Exception
}
