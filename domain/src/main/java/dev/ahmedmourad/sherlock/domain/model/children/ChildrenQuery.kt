package dev.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.model.auth.SimpleRetrievedUser
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Coordinates
import dev.ahmedmourad.sherlock.domain.model.children.submodel.ExactAppearance
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import kotlinx.serialization.Serializable

//TODO: add picture and lost date
@Serializable
@NoCopy
data class ChildrenQuery private constructor(
        val user: SimpleRetrievedUser,
        val fullName: FullName,
        val coordinates: Coordinates,
        val appearance: ExactAppearance,
        val page: Int
) {

    val timestamp = System.currentTimeMillis()

    fun toInvestigation() = Investigation.of(
            System.currentTimeMillis(),
            user,
            fullName,
            coordinates,
            appearance
    )

    companion object {

        fun of(user: SimpleRetrievedUser,
               fullName: FullName,
               coordinates: Coordinates,
               appearance: ExactAppearance,
               page: Int
        ): Either<Exception, ChildrenQuery> {
            return validate(user, fullName, coordinates, appearance, page)?.left()
                    ?: ChildrenQuery(user, fullName, coordinates, appearance, page).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(user: SimpleRetrievedUser,
                     fullName: FullName,
                     coordinates: Coordinates,
                     appearance: ExactAppearance,
                     page: Int
        ): Exception? {
            return if (page >= 0) {
                null
            } else {
                Exception.InvalidPageNumberException
            }
        }
    }

    sealed class Exception {
        object InvalidPageNumberException : Exception()
    }
}
