package dev.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class ApproximateAppearance private constructor(
        val gender: Gender?,
        val skin: Skin?,
        val hair: Hair?,
        val ageRange: AgeRange?,
        val heightRange: HeightRange?
) {

    companion object {

        fun of(
                gender: Gender?,
                skin: Skin?,
                hair: Hair?,
                age: AgeRange?,
                height: HeightRange?
        ): Either<Exception, ApproximateAppearance> {
            return validate(gender, skin, hair, age, height)?.left()
                    ?: ApproximateAppearance(gender, skin, hair, age, height).right()
        }

        fun validate(
                gender: Gender?,
                skin: Skin?,
                hair: Hair?,
                age: AgeRange?,
                height: HeightRange?
        ): Exception? {
            return if (gender != null || skin != null || hair != null || age != null || height != null) {
                null
            } else {
                Exception.NotEnoughDetailsException
            }
        }
    }

    sealed class Exception {
        object NotEnoughDetailsException : Exception()
    }
}
