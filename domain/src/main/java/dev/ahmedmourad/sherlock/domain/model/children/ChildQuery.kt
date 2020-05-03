package dev.ahmedmourad.sherlock.domain.model.children

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.children.submodel.ExactAppearance
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Location
import kotlinx.serialization.Serializable

//TODO: add picture and lost date
//TODO: add user id
@Serializable
@NoCopy
data class ChildQuery private constructor(
        val fullName: FullName,
        val location: Location,
        val appearance: ExactAppearance
) {
    companion object {
        fun of(fullName: FullName,
               location: Location,
               appearance: ExactAppearance
        ): ChildQuery {
            return ChildQuery(fullName, location, appearance)
        }
    }
}
