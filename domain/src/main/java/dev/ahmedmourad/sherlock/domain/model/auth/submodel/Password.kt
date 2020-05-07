package dev.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class Password private constructor(val value: String) {

    companion object {

        private const val MIN_LENGTH = 7
        private const val MIN_DISTINCT_CHARACTERS = 4

        fun of(value: String): Either<Exception, Password> {
            return validate(value)?.left() ?: Password(value).right()
        }

        fun validate(value: String): Exception? {
            val chars = value.toCharArray()
            return when {

                value.isBlank() -> Exception.BlankPasswordException

                value.length < MIN_LENGTH -> Exception.PasswordTooShortException(value.length, MIN_LENGTH)

                chars.none(Char::isUpperCase) -> Exception.NoCapitalLettersException

                chars.none(Char::isLowerCase) -> Exception.NoSmallLettersException

                chars.none(Char::isDigit) -> Exception.NoDigitsException

                chars.all(Char::isLetterOrDigit) -> Exception.NoSymbolsException

                chars.distinct().size < MIN_DISTINCT_CHARACTERS ->
                    Exception.FewDistinctCharactersException(chars.distinct().size, MIN_DISTINCT_CHARACTERS)

                chars.areSequential() -> Exception.OnlySequentialCharactersOrDigitsException

                else -> null
            }
        }

        private fun CharArray.areSequential(): Boolean {

            if (this.size < 3) {
                return false
            }

            val gap = this[1].toInt() - this[0].toInt()
            return this.asSequence()
                    .map(Char::toInt)
                    .drop(1)
                    .fold(true to this[0].toInt()) { (isSequential, prev), current ->
                        (isSequential && (current - prev == gap)) to current
                    }.first
        }
    }

    sealed class Exception {
        object BlankPasswordException : Exception()
        data class PasswordTooShortException(val length: Int, val minLength: Int) : Exception()
        object NoCapitalLettersException : Exception()
        object NoSmallLettersException : Exception()
        object NoDigitsException : Exception()
        object NoSymbolsException : Exception()
        data class FewDistinctCharactersException(val count: Int, val min: Int) : Exception()
        object OnlySequentialCharactersOrDigitsException : Exception()
    }
}
