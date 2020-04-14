package dev.ahmedmourad.sherlock.android.model.auth

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.android.utils.getImageBytes
import dev.ahmedmourad.sherlock.domain.exceptions.ModelConversionException
import dev.ahmedmourad.sherlock.domain.model.auth.SignUpUser
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import dev.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials
import dev.ahmedmourad.sherlock.domain.model.common.PicturePath
import timber.log.Timber
import timber.log.error

internal class AppSignUpUser private constructor(
        val credentials: UserCredentials,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picturePath: PicturePath?
) {

    fun component1() = credentials

    fun component2() = displayName

    fun component3() = phoneNumber

    fun component4() = picturePath

    fun toSignUpUser(): SignUpUser {
        return SignUpUser.of(
                credentials,
                displayName,
                phoneNumber,
                getImageBytes(picturePath)
        ).getOrHandle {
            Timber.error(ModelConversionException(it.toString()), it::toString)
            null
        }!!
    }

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as AppSignUpUser

        if (credentials != other.credentials)
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
        var result = credentials.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + picturePath.hashCode()
        return result
    }

    override fun toString(): String {
        return "AppSignUpUser(" +
                "credentials=$credentials, " +
                "displayName=$displayName, " +
                "phoneNumber=$phoneNumber, " +
                "picturePath=$picturePath" +
                ")"
    }

    companion object {
        fun of(credentials: UserCredentials,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picturePath: PicturePath?
        ): Either<SignUpUser.Exception, AppSignUpUser> {
            return SignUpUser.validate(
                    credentials,
                    displayName,
                    phoneNumber,
                    getImageBytes(picturePath)
            )?.left() ?: AppSignUpUser(credentials, displayName, phoneNumber, picturePath).right()
        }
    }
}
