package inc.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin

class ApproximateAppearance private constructor(
        val gender: Gender?,
        val skin: Skin?,
        val hair: Hair?,
        val ageRange: AgeRange?,
        val heightRange: HeightRange?
) {

    fun component1() = gender

    fun component2() = skin

    fun component3() = hair

    fun component4() = ageRange

    fun component5() = heightRange

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as ApproximateAppearance

        if (gender != other.gender)
            return false

        if (skin != other.skin)
            return false

        if (hair != other.hair)
            return false

        if (ageRange != other.ageRange)
            return false

        if (heightRange != other.heightRange)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = gender.hashCode()
        result = 31 * result + skin.hashCode()
        result = 31 * result + hair.hashCode()
        result = 31 * result + ageRange.hashCode()
        result = 31 * result + heightRange.hashCode()
        return result
    }

    override fun toString(): String {
        return "ApproximateAppearance(gender=$gender, skin=$skin, hair=$hair, ageRange=$ageRange, heightRange=$heightRange)"
    }

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
