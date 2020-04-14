package dev.ahmedmourad.sherlock.platform.dagger.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import dev.ahmedmourad.sherlock.domain.platform.LocationManager
import dev.ahmedmourad.sherlock.domain.platform.TextManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidConnectivityManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidDateManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidLocationManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidTextManager

@Module
internal object DateManagerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): DateManager = AndroidDateManager()
}

@Module
internal object LocationManagerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): LocationManager = AndroidLocationManager()
}

@Module
internal object TextManagerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): TextManager = AndroidTextManager()
}

@Module
internal object ConnectivityManagerModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): ConnectivityManager = AndroidConnectivityManager()
}
