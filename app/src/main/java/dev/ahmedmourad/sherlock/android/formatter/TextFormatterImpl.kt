package dev.ahmedmourad.sherlock.android.formatter

import android.annotation.SuppressLint
import arrow.core.Either
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.R
import dev.ahmedmourad.sherlock.domain.constants.Gender
import dev.ahmedmourad.sherlock.domain.constants.Hair
import dev.ahmedmourad.sherlock.domain.constants.Skin
import dev.ahmedmourad.sherlock.domain.model.children.submodel.*
import dev.ahmedmourad.sherlock.domain.model.common.Name
import dev.ahmedmourad.sherlock.domain.platform.TextManager
import splitties.init.appCtx
import java.util.*
import javax.inject.Inject

@Reusable
internal class TextFormatterImpl @Inject constructor(
        private val textManager: TextManager
) : TextFormatter {

    override fun formatSkin(skin: Skin?): String {
        return skin?.getMessage(textManager) ?: appCtx.getString(R.string.not_available)
    }

    override fun formatHair(hair: Hair?): String {
        return hair?.getMessage(textManager) ?: appCtx.getString(R.string.not_available)
    }

    override fun formatGender(gender: Gender?): String {
        return gender?.getMessage(textManager) ?: appCtx.getString(R.string.not_available)
    }

    override fun formatName(name: Either<Name, FullName>?): String {
        return when (name) {
            is Either.Left -> name.a.value
            is Either.Right -> "${name.b.first.value} ${name.b.last.value}"
            else -> appCtx.getString(R.string.not_available)
        }
    }

    override fun formatNotes(notes: String?): String {
        return if (notes != null && notes.isNotBlank()) {
            notes
        } else {
            appCtx.getString(R.string.not_available)
        }
    }

    override fun formatAge(age: AgeRange?): String {
        return if (age != null) {
            appCtx.getString(R.string.years_range, "${age.min} - ${age.max}")
        } else {
            appCtx.getString(R.string.not_available)
        }
    }

    override fun formatLocation(location: Location?): String {
        return if (location != null) {
            formatLocation(location.name, location.address)
        } else {
            appCtx.getString(R.string.not_available)
        }
    }

    override fun formatLocation(locationName: String?, locationAddress: String?): String {

        val isNameEmpty = locationName == null || locationName.isBlank()
        val isAddressEmpty = locationAddress == null || locationAddress.isBlank()

        return when {

            !isNameEmpty && !isAddressEmpty -> {
                appCtx.getString(R.string.location, locationName, locationAddress)
            }

            !isNameEmpty -> locationName!!

            !isAddressEmpty -> locationAddress!!

            else -> appCtx.getString(R.string.not_available)
        }
    }

    @ExperimentalStdlibApi
    override fun formatHeight(height: HeightRange?): String {
        return if (height != null) {
            appCtx.getString(R.string.height_range,
                    formatHeight(height.min),
                    formatHeight(height.max)
            )
        } else {
            appCtx.getString(R.string.not_available)
        }
    }

    @SuppressLint("DefaultLocale")
    @ExperimentalStdlibApi
    private fun formatHeight(height: Height): String {

        var result = ""

        val meters = height.value / 100
        val centimeters = height.value % 100

        if (meters > 0)
            result += appCtx.resources.getQuantityString(R.plurals.height_meters, meters, meters)

        if (meters > 0 && centimeters > 0)
            result += " "

        if (centimeters > 0)
            result += appCtx.resources.getQuantityString(R.plurals.height_centimeters, centimeters, centimeters)

        return if (result.isBlank()) appCtx.getString(R.string.not_available) else result.capitalize(Locale.getDefault())
    }
}
