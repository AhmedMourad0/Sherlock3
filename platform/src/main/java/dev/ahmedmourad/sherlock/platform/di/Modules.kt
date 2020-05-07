package dev.ahmedmourad.sherlock.platform.di

import dagger.Binds
import dagger.Module
import dev.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import dev.ahmedmourad.sherlock.domain.platform.LocationManager
import dev.ahmedmourad.sherlock.domain.platform.TextManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidConnectivityManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidDateManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidLocationManager
import dev.ahmedmourad.sherlock.platform.managers.AndroidTextManager

@Module
internal interface PlatformBindingsModule {

    @Binds
    fun bindDateManager(
            impl: AndroidDateManager
    ): DateManager

    @Binds
    fun bindLocationManager(
            impl: AndroidLocationManager
    ): LocationManager

    @Binds
    fun bindTextManager(
            impl: AndroidTextManager
    ): TextManager

    @Binds
    fun bindConnectivityManager(
            impl: AndroidConnectivityManager
    ): ConnectivityManager
}
