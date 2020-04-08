package inc.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable

@Serializable
class Coordinates private constructor(val latitude: Double, val longitude: Double) {

    fun component1() = latitude

    fun component2() = longitude

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Coordinates

        if (latitude != other.latitude)
            return false

        if (longitude != other.longitude)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

    override fun toString(): String {
        return "Coordinates(latitude='$latitude', longitude='$longitude')"
    }

    companion object {

        fun of(latitude: Double, longitude: Double): Either<Exception, Coordinates> {
            return validate(latitude, longitude)?.left() ?: Coordinates(latitude, longitude).right()
        }

        fun validate(latitude: Double, longitude: Double): Exception? {

            val isLatitudeValid = latitude between (-90.0 to 90.0)
            val isLongitudeValid = longitude between (-180.0 to 180.0)

            return if (!isLatitudeValid && !isLongitudeValid) {
                Exception.InvalidCoordinatesException
            } else if (!isLatitudeValid) {
                Exception.InvalidLatitudeException
            } else if (!isLongitudeValid) {
                Exception.InvalidLongitudeException
            } else {
                null
            }
        }
    }

    sealed class Exception {
        object InvalidLatitudeException : Exception()
        object InvalidLongitudeException : Exception()
        object InvalidCoordinatesException : Exception()
    }
}

private infix fun Double.between(pair: Pair<Double, Double>) = this >= pair.first && this <= pair.second
