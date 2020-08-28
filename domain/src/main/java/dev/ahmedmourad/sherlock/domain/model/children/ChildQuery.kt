package dev.ahmedmourad.sherlock.domain.model.children

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.submodel.ExactAppearance
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Location
import kotlinx.serialization.Serializable

//TODO: add picture and lost date
@Serializable
@NoCopy
data class ChildQuery private constructor(
        val user: SimpleRetrievedUser,
        val fullName: FullName,
        val location: Location,
        val appearance: ExactAppearance
) {
    companion object {
        fun of(user: SimpleRetrievedUser,
               fullName: FullName,
               location: Location,
               appearance: ExactAppearance
        ): ChildQuery {
            return ChildQuery(user, fullName, location, appearance)
        }
    }
}
