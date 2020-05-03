package dev.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class AgeRange private constructor(val min: Age, val max: Age) {

    companion object {

        fun of(min: Age, max: Age): Either<Exception, AgeRange> {
            return validate(min, max)?.left() ?: AgeRange(min, max).right()
        }

        fun validate(min: Age, max: Age): Exception? {
            return if (min.value < max.value) {
                null
            } else {
                Exception.MinExceedsMaxException(min, max)
            }
        }
    }

    sealed class Exception {
        data class MinExceedsMaxException(val min: Age, val max: Age) : Exception()
    }
}
