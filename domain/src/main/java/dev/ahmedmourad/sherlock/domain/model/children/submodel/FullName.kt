package dev.ahmedmourad.sherlock.domain.model.children.submodel

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.common.Name
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class FullName private constructor(val first: Name, val last: Name) {
    companion object {
        fun of(first: Name, last: Name): FullName {
            return FullName(first, last)
        }
    }
}
