package dev.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.sherlock.domain.model.common.Name
import kotlinx.serialization.Serializable

@Serializable
class DisplayName private constructor(val value: String) {

    fun component1() = value

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as DisplayName

        if (value != other.value)
            return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "DisplayName(value='$value')"
    }

    companion object {

        private const val MAX_NAMES_COUNT = 3
        const val MIN_LENGTH = Name.MIN_LENGTH
        const val MAX_LENGTH = Name.MAX_LENGTH * MAX_NAMES_COUNT + (MAX_NAMES_COUNT - 1)

        fun of(value: String): Either<Exception, DisplayName> {
            return validate(value)?.left() ?: DisplayName(value.trim()).right()
        }

        fun validate(value: String): Exception? {
            val trimmedName = value.trim()
            return if (value.isBlank()) {
                Exception.BlankDisplayNameException
            } else if (trimmedName.length < MIN_LENGTH) {
                Exception.DisplayNameTooShortException(trimmedName.length, MIN_LENGTH)
            } else if (trimmedName.length > MAX_LENGTH) {
                Exception.DisplayNameTooLongException(trimmedName.length, MAX_LENGTH)
            } else if (!trimmedName.replace(" ", "").toCharArray().all(Char::isLetter)) {
                Exception.DisplayNameContainsNumbersOrSymbolsException
            } else {

                val illegalName = value.split(" ")
                        .mapNotNull { name -> (Name.of(name) as? Either.Left)?.let { name to it } }
                        .firstOrNull()

                if (illegalName != null) {
                    Exception.SingleNameException(illegalName.first, illegalName.second.a)
                } else {
                    null
                }
            }
        }
    }

    sealed class Exception {
        object BlankDisplayNameException : Exception()
        data class DisplayNameTooShortException(val length: Int, val minLength: Int) : Exception()
        data class DisplayNameTooLongException(val length: Int, val maxLength: Int) : Exception()
        object DisplayNameContainsNumbersOrSymbolsException : Exception()
        data class SingleNameException(val value: String, val exception: Name.Exception) : Exception()
    }
}
