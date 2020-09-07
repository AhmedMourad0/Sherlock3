package dev.ahmedmourad.sherlock.domain.model.children

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Coordinates
import dev.ahmedmourad.sherlock.domain.model.children.submodel.ExactAppearance
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import kotlinx.serialization.Serializable

//TODO: add picture and lost date
@Serializable
@NoCopy
data class Investigation private constructor(
        val timestamp: Long,
        val user: SimpleRetrievedUser,
        val fullName: FullName,
        val coordinates: Coordinates,
        val appearance: ExactAppearance
) {
    companion object {
        fun of(timestamp: Long,
               user: SimpleRetrievedUser,
               fullName: FullName,
               coordinates: Coordinates,
               appearance: ExactAppearance
        ): Investigation {
            return Investigation(timestamp, user, fullName, coordinates, appearance)
        }
    }
}
