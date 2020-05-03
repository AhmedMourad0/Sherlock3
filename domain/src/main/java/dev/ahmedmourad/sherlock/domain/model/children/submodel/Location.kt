package dev.ahmedmourad.sherlock.domain.model.children.submodel

import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class Location private constructor(
        val id: String,
        val name: String,
        val address: String,
        val coordinates: Coordinates
) {
    companion object {
        fun of(id: String, name: String, address: String, coordinates: Coordinates): Location {
            return Location(id, name, address, coordinates)
        }
    }
}
