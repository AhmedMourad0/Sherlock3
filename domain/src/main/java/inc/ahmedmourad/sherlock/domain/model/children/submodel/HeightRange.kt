package inc.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable

@Serializable
class HeightRange private constructor(val min: Height, val max: Height) {

    fun component1() = min

    fun component2() = max

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as HeightRange

        if (min != other.min)
            return false

        if (max != other.max)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = min.hashCode()
        result = 31 * result + max.hashCode()
        return result
    }

    override fun toString(): String {
        return "HeightRange(min=$min, max=$max)"
    }

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
