package inc.ahmedmourad.sherlock.domain.model.children.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin
import kotlinx.serialization.Serializable

@Serializable
class ExactAppearance private constructor(
        val gender: Gender,
        val skin: Skin,
        val hair: Hair,
        val age: Age,
        val height: Height
) {

    fun component1() = gender

    fun component2() = skin

    fun component3() = hair

    fun component4() = age

    fun component5() = height

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as ExactAppearance

        if (gender != other.gender)
            return false

        if (skin != other.skin)
            return false

        if (hair != other.hair)
            return false

        if (age != other.age)
            return false

        if (height != other.height)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = gender.hashCode()
        result = 31 * result + skin.hashCode()
        result = 31 * result + hair.hashCode()
        result = 31 * result + age.hashCode()
        result = 31 * result + height.hashCode()
        return result
    }

    override fun toString(): String {
        return "ExactAppearance(gender=$gender, skin=$skin, hair=$hair, age=$age, height=$height)"
    }

    companion object {

        fun of(gender: Gender, skin: Skin, hair: Hair, age: Age, height: Height): Either<Exception, ExactAppearance> {
            return validate(gender, skin, hair, age, height)?.left()
                    ?: ExactAppearance(gender, skin, hair, age, height).right()
        }

        @Suppress("UNUSED_PARAMETER")
        fun validate(gender: Gender, skin: Skin, hair: Hair, age: Age, height: Height): Exception? {
            return null
        }
    }

    sealed class Exception
}

