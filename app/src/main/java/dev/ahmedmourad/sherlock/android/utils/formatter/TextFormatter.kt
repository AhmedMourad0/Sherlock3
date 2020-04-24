package dev.ahmedmourad.sherlock.android.utils.formatter

import arrow.core.Either
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.model.children.submodel.AgeRange
import dev.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import dev.ahmedmourad.sherlock.domain.model.children.submodel.HeightRange
import dev.ahmedmourad.sherlock.domain.model.children.submodel.Location
import dev.ahmedmourad.sherlock.domain.model.common.Name

internal interface TextFormatter {

    fun formatSkin(skin: Skin?): String

    fun formatHair(hair: Hair?): String

    fun formatGender(gender: Gender?): String

    fun formatName(name: Either<Name, FullName>?): String

    fun formatNotes(notes: String?): String

    fun formatAge(age: AgeRange?): String

    fun formatLocation(location: Location?): String

    fun formatLocation(locationName: String?, locationAddress: String?): String

    fun formatHeight(height: HeightRange?): String
}
