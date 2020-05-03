package dev.ahmedmourad.sherlock.domain.model.common

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class Name private constructor(val value: String) {

    companion object {

        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 10

        fun of(value: String): Either<Exception, Name> {
            return validate(value)?.left() ?: Name(value.trim()).right()

        }

        fun validate(value: String): Exception? {
            val trimmedValue = value.trim()
            return if (value.isBlank()) {
                Exception.BlankNameException
            } else if (trimmedValue.contains(" ")) {
                Exception.NameContainsWhiteSpacesException
            } else if (trimmedValue.length < MIN_LENGTH) {
                Exception.NameTooShortException(trimmedValue.length, MIN_LENGTH)
            } else if (trimmedValue.length > MAX_LENGTH) {
                Exception.NameTooLongException(trimmedValue.length, MAX_LENGTH)
            } else if (!trimmedValue.toCharArray().all(Char::isLetter)) {
                Exception.NameContainsNumbersOrSymbolsException
            } else {
                null
            }
        }
    }

    sealed class Exception {
        object BlankNameException : Exception()
        object NameContainsWhiteSpacesException : Exception()
        data class NameTooShortException(val length: Int, val minLength: Int) : Exception()
        data class NameTooLongException(val length: Int, val maxLength: Int) : Exception()
        object NameContainsNumbersOrSymbolsException : Exception()
    }
}
