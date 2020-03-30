package inc.ahmedmourad.sherlock.model.common

import androidx.annotation.DrawableRes
import dagger.Lazy

internal data class AppSection(
        val name: String,
        @DrawableRes val imageDrawable: Int,
        val controller: Lazy<out TaggedController>?
)
