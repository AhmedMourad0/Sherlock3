package inc.ahmedmourad.sherlock.platform.dagger.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.domain.platform.ConnectivityManager
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.domain.platform.LocationManager
import inc.ahmedmourad.sherlock.domain.platform.TextManager
import inc.ahmedmourad.sherlock.platform.managers.AndroidConnectivityManager
import inc.ahmedmourad.sherlock.platform.managers.AndroidDateManager
import inc.ahmedmourad.sherlock.platform.managers.AndroidLocationManager
import inc.ahmedmourad.sherlock.platform.managers.AndroidTextManager

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
