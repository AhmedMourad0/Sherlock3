package inc.ahmedmourad.sherlock.platform.dagger

import dagger.Module
import inc.ahmedmourad.sherlock.platform.dagger.modules.ConnectivityManagerModule
import inc.ahmedmourad.sherlock.platform.dagger.modules.DateManagerModule
import inc.ahmedmourad.sherlock.platform.dagger.modules.LocationManagerModule
import inc.ahmedmourad.sherlock.platform.dagger.modules.TextManagerModule

@Module(includes = [
    DateManagerModule::class,
    LocationManagerModule::class,
    TextManagerModule::class,
    ConnectivityManagerModule::class
])
object PlatformModule
