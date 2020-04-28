package dev.ahmedmourad.sherlock.domain.model.children.submodel

import dev.ahmedmourad.sherlock.domain.model.common.Name
import kotlinx.serialization.Serializable

@Serializable
class FullName private constructor(val first: Name, val last: Name) {

    fun component1() = first

    fun component2() = last

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as FullName

        if (first != other.first)
            return false

        if (last != other.last)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + last.hashCode()
        return result
    }

    override fun toString(): String {
        return "FullName(first='$first', last='$last')"
    }

    companion object {
        fun of(first: Name, last: Name): FullName {
            return FullName(first, last)
        }
    }
}
