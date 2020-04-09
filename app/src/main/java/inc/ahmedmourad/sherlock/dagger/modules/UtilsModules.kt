package inc.ahmedmourad.sherlock.dagger.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.domain.platform.TextManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter
import inc.ahmedmourad.sherlock.utils.formatter.TextFormatter
import inc.ahmedmourad.sherlock.utils.pickers.images.EsafirmImagePicker
import inc.ahmedmourad.sherlock.utils.pickers.images.ImagePicker
import inc.ahmedmourad.sherlock.utils.pickers.places.GooglePlacePicker
import inc.ahmedmourad.sherlock.utils.pickers.places.PlacePicker

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
