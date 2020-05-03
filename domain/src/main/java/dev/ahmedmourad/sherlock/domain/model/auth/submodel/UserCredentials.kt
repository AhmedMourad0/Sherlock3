package dev.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class UserCredentials private constructor(
        val email: Email,
        val password: Password
) {

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
