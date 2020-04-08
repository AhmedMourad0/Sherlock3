package inc.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable

@Serializable
class Location private constructor(
        val id: String,
        val name: String,
        val address: String,
        val coordinates: Coordinates
) {

    fun component1() = id

    fun component2() = name

    fun component3() = address

    fun component4() = coordinates

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Location

        if (id != other.id)
            return false

        if (name != other.name)
            return false

        if (address != other.address)
            return false

        if (coordinates != other.coordinates)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + coordinates.hashCode()
        return result
    }

    override fun toString(): String {
        return "Location(id='$id', name='$name', address='$address', coordinates=$coordinates)"
    }

    companion object {

        fun of(id: String, name: String, address: String, coordinates: Coordinates): Either<Exception, Location> {
            return validate(id, name, address, coordinates)?.left()
                    ?: Location(id, name, address, coordinates).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(id: String, name: String, address: String, coordinates: Coordinates): Exception? {
            return null
        }
    }

    sealed class Exception
}
