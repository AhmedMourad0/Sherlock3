package inc.ahmedmourad.sherlock.domain.model.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.DisplayName
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.PhoneNumber
import inc.ahmedmourad.sherlock.domain.model.auth.submodel.UserCredentials

class SignUpUser private constructor(
        val credentials: UserCredentials,
        val displayName: DisplayName,
        val phoneNumber: PhoneNumber,
        val picture: ByteArray?
) {
    fun component1() = credentials

    fun component2() = displayName

    fun component3() = phoneNumber

    fun component4() = picture

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as SignUpUser

        if (credentials != other.credentials)
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
        var result = credentials.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + (picture?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SignUpUser(" +
                "credentials=$credentials, " +
                "displayName=$displayName, " +
                "phoneNumber=$phoneNumber, " +
                "picture=${picture?.contentToString()}" +
                ")"
    }

    companion object {

        fun of(credentials: UserCredentials,
               displayName: DisplayName,
               phoneNumber: PhoneNumber,
               picture: ByteArray?
        ): Either<Exception, SignUpUser> {
            return validate(credentials, displayName, phoneNumber, picture)?.left()
                    ?: SignUpUser(credentials, displayName, phoneNumber, picture).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(credentials: UserCredentials,
                     displayName: DisplayName,
                     phoneNumber: PhoneNumber,
                     picture: ByteArray?
        ): Exception? {
            return when {

                displayName.value == credentials.password.value ->
                    Exception.DisplayNameIsUsedAsPasswordException

                credentials.password.value in arrayOf(
                        phoneNumber.number,
                        phoneNumber.countryCode + phoneNumber.number
                ) -> Exception.PhoneNumberIsUsedAsPasswordException

                else -> null
            }
        }
    }

    sealed class Exception {
        object DisplayNameIsUsedAsPasswordException : Exception()
        object PhoneNumberIsUsedAsPasswordException : Exception()
    }
}
