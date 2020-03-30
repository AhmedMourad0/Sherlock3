package inc.ahmedmourad.sherlock.domain.dagger.modules

import dagger.Module
import dagger.Provides
import inc.ahmedmourad.sherlock.domain.bus.Bus
import inc.ahmedmourad.sherlock.domain.bus.RxBus
import javax.inject.Singleton

@Module
internal object BusModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideBus(): Bus = RxBus()
}
