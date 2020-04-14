package dev.ahmedmourad.sherlock.android.dagger.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.utils.formatter.Formatter
import dev.ahmedmourad.sherlock.android.utils.formatter.TextFormatter
import dev.ahmedmourad.sherlock.android.utils.pickers.images.EsafirmImagePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.images.ImagePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.places.GooglePlacePicker
import dev.ahmedmourad.sherlock.android.utils.pickers.places.PlacePicker
import dev.ahmedmourad.sherlock.domain.platform.TextManager

@Module
internal object PlacePickerModule {
    @Provides
    @JvmStatic
    fun provide(): PlacePicker = GooglePlacePicker()
}

@Module
internal object ImagePickerModule {
    @Provides
    @JvmStatic
    fun provide(): ImagePicker = EsafirmImagePicker()
}

@Module
internal object TextFormatterModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(textManager: TextManager): Formatter = TextFormatter(textManager)
}
