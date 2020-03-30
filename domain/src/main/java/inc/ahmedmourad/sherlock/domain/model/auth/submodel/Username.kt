package inc.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.exceptions.ModelCreationException
import timber.log.Timber
import timber.log.error
import kotlin.math.pow

class Username private constructor(val value: String) {

    fun component1() = value

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Username

        if (value != other.value)
            return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Username(value='$value')"
    }

    companion object {

        private const val SUFFIX_LENGTH = 5
        private const val MIN_LENGTH = DisplayName.MIN_LENGTH
        private const val MAX_LENGTH_WITHOUT_SUFFIX = DisplayName.MAX_LENGTH
        private const val MAX_LENGTH = MAX_LENGTH_WITHOUT_SUFFIX + SUFFIX_LENGTH

        fun from(displayName: DisplayName): Username {
            return if (displayName.value.replace(" ", "").matches(Regex("[a-zA-Z]+"))) {
                val suffix = System.currentTimeMillis() % 10.0.pow(SUFFIX_LENGTH).toLong()
                of(displayName.value.replace(" ", "_") + suffix)
            } else {
                of(createRandomUsername())
            }.getOrHandle {
                Timber.error(ModelCreationException(it.toString()), it::toString)
                null
            }!!
        }

        private fun createRandomUsername(): String {

            val underscoresMaxLength = 2
            val lettersMaxLength = MAX_LENGTH_WITHOUT_SUFFIX / 2 - underscoresMaxLength
            val numbersMaxLength = SUFFIX_LENGTH

            val allowedCharacters = ('a'..'z') + ('A'..'Z')
            val lettersLength = ((lettersMaxLength - 2)..lettersMaxLength).random()
            val letters = (1..lettersLength).map { allowedCharacters.random() }
            val underscoresLength = (1..underscoresMaxLength).random()
            val underscores = (1..underscoresLength).map { '_' }
            val numbersLength = ((numbersMaxLength - 2)..numbersMaxLength).random()
            val numbers = 10.0.pow(numbersLength).toLong() + (0..10.0.pow(numbersLength - 1).toLong()).random()

            val username = letters + underscores + numbers.toString().toCharArray().toList()

            return username.shuffled().joinToString("")
        }

        fun of(value: String): Either<Exception, Username> {
            return validate(value)?.left() ?: Username(value.trim()).right()
        }

        fun validate(value: String): Exception? {
            val trimmedValue = value.trim()
            return when {

                trimmedValue.isBlank() -> Exception.BlankUsernameException

                trimmedValue.contains(" ") -> Exception.UsernameContainsWhiteSpacesException

                trimmedValue.length < MIN_LENGTH -> Exception.UsernameTooShortException(trimmedValue.length, MIN_LENGTH)

                trimmedValue.length > MAX_LENGTH -> Exception.UsernameTooLongException(trimmedValue.length, MAX_LENGTH)

                trimmedValue.replace("_", "").all(Character::isLetterOrDigit).not() ->
                    Exception.UsernameContainsNonUnderscoreSymbolsException

                trimmedValue.replace("_", "").isBlank() ->
                    Exception.UsernameOnlyContainsUnderscoresException

                trimmedValue.all(Character::isDigit) -> Exception.UsernameOnlyContainsDigitsException

                trimmedValue.none(Character::isLetter) -> Exception.NoLettersException

                trimmedValue.any { it.isLetter() && it.toLowerCase() !in 'a'..'z' } ->
                    Exception.UsernameContainsNonEnglishLettersException

                else -> null
            }
        }
    }

    sealed class Exception {
        object BlankUsernameException : Exception()
        object UsernameContainsWhiteSpacesException : Exception()
        data class UsernameTooShortException(val length: Int, val minLength: Int) : Exception()
        data class UsernameTooLongException(val length: Int, val maxLength: Int) : Exception()
        object UsernameContainsNonUnderscoreSymbolsException : Exception()
        object UsernameOnlyContainsUnderscoresException : Exception()
        object UsernameOnlyContainsDigitsException : Exception()
        object NoLettersException : Exception()
        object UsernameContainsNonEnglishLettersException : Exception()
    }
}
