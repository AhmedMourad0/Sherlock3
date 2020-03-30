package inc.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right

class UserCredentials private constructor(
        val email: Email,
        val password: Password
) {

    fun component1() = email

    fun component2() = password

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as UserCredentials

        if (email != other.email)
            return false

        if (password != other.password)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = email.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }

    override fun toString(): String {
        return "SignUpUser(" +
                "email=$email, " +
                "password=$password" +
                ")"
    }

    companion object {

        fun of(email: Email, password: Password): Either<Exception, UserCredentials> {
            return validate(email, password)?.left() ?: UserCredentials(email, password).right()
        }

        fun validate(email: Email, password: Password): Exception? {
            return when (password.value) {
                email.value -> Exception.EmailIsUsedAsPasswordException
                else -> null
            }
        }
    }

    sealed class Exception {
        object EmailIsUsedAsPasswordException : Exception()
    }
}
