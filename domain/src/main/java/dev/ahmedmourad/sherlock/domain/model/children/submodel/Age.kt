package dev.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class Age private constructor(val value: Int) {

    companion object {

        private const val MIN_VALUE = 0
        private const val MAX_VALUE = 200

        fun of(value: Int): Either<Exception, Age> {
            return validate(value)?.left() ?: Age(value).right()
        }

        fun validate(value: Int): Exception? {
            return if (value in MIN_VALUE..MAX_VALUE) {
                null
            } else {
                Exception.AgeOutOfRangeException(value, MIN_VALUE, MAX_VALUE)
            }
        }
    }

    sealed class Exception {
        data class AgeOutOfRangeException(val value: Int, val min: Int, val max: Int) : Exception()
    }
}
