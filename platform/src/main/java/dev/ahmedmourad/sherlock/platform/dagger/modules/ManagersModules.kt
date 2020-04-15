package dev.ahmedmourad.sherlock.platform.dagger.modules

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
internal interface ManagersModule {

    @Binds
    fun bind(dateManager: AndroidDateManager): DateManager

    @Binds
    fun bind(locationManager: AndroidLocationManager): LocationManager

    @Binds
    fun bind(textManager: AndroidTextManager): TextManager

    @Binds
    fun bind(connectivityManager: AndroidConnectivityManager): ConnectivityManager
}
