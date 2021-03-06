package dev.ahmedmourad.sherlock.android.model.common

import androidx.annotation.DrawableRes
import androidx.navigation.NavDirections

internal data class AppSection(
        val name: String,
        @DrawableRes val imageDrawable: Int,
        val navDirectionFactory: (() -> NavDirections)?
)
