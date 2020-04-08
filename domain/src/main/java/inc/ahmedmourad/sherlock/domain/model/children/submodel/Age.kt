package inc.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable

@Serializable
class Age private constructor(val value: Int) {

    fun component1() = value

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Age

        if (value != other.value)
            return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return "Age(value=$value)"
    }

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
