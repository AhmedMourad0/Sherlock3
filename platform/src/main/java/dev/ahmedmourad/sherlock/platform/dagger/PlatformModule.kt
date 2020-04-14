package dev.ahmedmourad.sherlock.platform.dagger

import dagger.Module
import dev.ahmedmourad.sherlock.platform.dagger.modules.ConnectivityManagerModule
import dev.ahmedmourad.sherlock.platform.dagger.modules.DateManagerModule
import dev.ahmedmourad.sherlock.platform.dagger.modules.LocationManagerModule
import dev.ahmedmourad.sherlock.platform.dagger.modules.TextManagerModule

@Module(includes = [
    DateManagerModule::class,
    LocationManagerModule::class,
    TextManagerModule::class,
    ConnectivityManagerModule::class
])
object PlatformModule
