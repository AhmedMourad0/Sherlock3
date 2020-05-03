package dev.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class Coordinates private constructor(val latitude: Double, val longitude: Double) {

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
