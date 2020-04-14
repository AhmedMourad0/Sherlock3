package dev.ahmedmourad.sherlock.android.model.auth

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.android.utils.getImageBytes
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.auth.CompletedUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.Email
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import dev.ahmedmourad.sherlock.domain.model.ids.UserId
import timber.log.Timber
import timber.log.error

internal class AppCompletedUser private constructor(
        val id: UserId,
        val email: Email,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picturePath: PicturePath?
) {

    fun toCompletedUser(): CompletedUser {
        return CompletedUser.of(
                id,
                email,
                displayName,
                phoneNumber,
                getImageBytes(picturePath)
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }

    fun component1() = id

    fun component2() = email

    fun component3() = displayName

    fun component4() = phoneNumber

    fun component5() = picturePath

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as AppCompletedUser

        if (id != other.id)
            return false

        if (email != other.email)
            return false

        if (displayName != other.displayName)
            return false

        if (phoneNumber != other.phoneNumber)
            return false

        if (picturePath != other.picturePath)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + picturePath.hashCode()
        return result
    }

    override fun toString(): String {
        return "AppCompletedUser(" +
                "id=$id, " +
                "email=$email, " +
                "displayName=$displayName, " +
                "phoneNumber=$phoneNumber, " +
                "picturePath=$picturePath" +
                ")"
    }

    companion object {
        fun of(id: UserId,
               email: Email,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picturePath: PicturePath?
        ): Either<CompletedUser.Exception, AppCompletedUser> {
            return CompletedUser.validate(id, email, displayName, phoneNumber, getImageBytes(picturePath))?.left()
                    ?: AppCompletedUser(id, email, displayName, phoneNumber, picturePath).right()
        }
    }
}
