package dev.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.LeastVisibleCopy
import kotlinx.serialization.Serializable

@Serializable
@LeastVisibleCopy
data class Weight private constructor(val value: Double) {

    companion object {

        private const val MIN_VALUE = 0.0
        private const val MAX_VALUE = 1.0

        fun of(value: Double): Either<Exception, Weight> {
            return validate(value)?.left() ?: Weight(value).copy(value = value).right()
        }

        fun validate(value: Double): Exception? {
            return if (value in MIN_VALUE..MAX_VALUE) {
                null
            } else {
                Exception.WeightOutOfRangeException(value, MIN_VALUE, MAX_VALUE)
            }
        }
    }

    sealed class Exception {
        data class WeightOutOfRangeException(val value: Double, val min: Double, val max: Double) : Exception()
    }
}
