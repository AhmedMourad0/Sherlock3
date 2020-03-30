package inc.ahmedmourad.sherlock.domain.model.children

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.model.children.submodel.ExactAppearance
import inc.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location

//TODO: add picture and lost date
//TODO: add user id
class ChildQuery private constructor(
        val fullName: FullName,
        val location: Location,
        val appearance: ExactAppearance
) {

    fun component1() = fullName

    fun component2() = location

    fun component3() = appearance

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as ChildQuery

        if (fullName != other.fullName)
            return false

        if (location != other.location)
            return false

        if (appearance != other.appearance)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = fullName.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + appearance.hashCode()
        return result
    }

    override fun toString(): String {
        return "ChildQuery(fullName=$fullName, location=$location, appearance=$appearance)"
    }

    companion object {

        fun of(fullName: FullName,
               location: Location,
               appearance: ExactAppearance
        ): Either<Exception, ChildQuery> {
            return validate(fullName, location, appearance)?.left()
                    ?: ChildQuery(fullName, location, appearance).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(fullName: FullName,
                     location: Location,
                     appearance: ExactAppearance
        ): Exception? {
            return null
        }
    }

    sealed class Exception
}
