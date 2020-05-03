package dev.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class Height private constructor(val value: Int) {

    companion object {

        private const val MIN_VALUE = 20
        private const val MAX_VALUE = 300

        fun of(value: Int): Either<Exception, Height> {
            return validate(value)?.left() ?: Height(value).right()
        }

        fun validate(value: Int): Exception? {
            return if (value in MIN_VALUE..MAX_VALUE) {
                null
            } else {
                Exception.HeightOutOfRangeException(value, MIN_VALUE, MAX_VALUE)
            }
        }
    }

    sealed class Exception {
        data class HeightOutOfRangeException(val value: Int, val min: Int, val max: Int) : Exception()
    }
}
