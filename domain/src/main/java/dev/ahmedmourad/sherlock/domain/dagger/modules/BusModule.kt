package dev.ahmedmourad.sherlock.domain.dagger.modules

import dagger.Module
import dagger.Provides
import dev.ahmedmourad.sherlock.domain.bus.Bus
import dev.ahmedmourad.sherlock.domain.bus.RxBus
import javax.inject.Singleton

@Module
internal object BusModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provide(): Bus = RxBus()
}
