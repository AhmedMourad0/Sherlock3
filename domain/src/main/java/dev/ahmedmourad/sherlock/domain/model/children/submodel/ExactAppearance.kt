package dev.ahmedmourad.sherlock.domain.model.children.submodel

import dev.ahmedmourad.nocopy.annotations.NoCopy
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import kotlinx.serialization.Serializable

@Serializable
@NoCopy
data class ExactAppearance private constructor(
        val gender: Gender,
        val skin: Skin,
        val hair: Hair,
        val age: Age,
        val height: Height
) {
    companion object {
        fun of(gender: Gender, skin: Skin, hair: Hair, age: Age, height: Height): ExactAppearance {
            return ExactAppearance(gender, skin, hair, age, height)
        }
    }
}

