package dev.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class HeightRange private constructor(val min: Height, val max: Height) {

    companion object {

        fun of(min: Height, max: Height): Either<Exception, HeightRange> {
            return validate(min, max)?.left() ?: HeightRange(min, max).right()
        }

        fun validate(min: Height, max: Height): Exception? {
            return if (min.value < max.value) {
                null
            } else {
                Exception.MinExceedsMaxException(min, max)
            }
        }
    }

    sealed class Exception {
        data class MinExceedsMaxException(val min: Height, val max: Height) : Exception()
    }
}
