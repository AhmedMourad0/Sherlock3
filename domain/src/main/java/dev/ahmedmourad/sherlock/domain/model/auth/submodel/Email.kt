package dev.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class Email private constructor(val value: String) {

    companion object {

        private val EMAIL_REGEX =
                Regex("""[a-zA-Z0-9+._%\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})+""")

        fun of(value: String): Either<Exception, Email> {
            return validate(value)?.left() ?: Email(value.trim()).right()
        }

        fun validate(value: String): Exception? {
            return when {
                value.isBlank() -> Exception.BlankEmailException
                value.trim().contains(" ") -> Exception.EmailContainsWhiteSpacesException(value)
                !EMAIL_REGEX.matches(value) -> Exception.MalformedEmailException(value)
                else -> null
            }
        }
    }

    sealed class Exception {
        object BlankEmailException : Exception()
        data class EmailContainsWhiteSpacesException(val value: String) : Exception()
        data class MalformedEmailException(val value: String) : Exception()
    }
}
