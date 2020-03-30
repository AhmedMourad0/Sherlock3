package inc.ahmedmourad.sherlock.dagger.modules

import arrow.syntax.function.curried
import arrow.syntax.function.uncurried
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRemoteViewsFactoryFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRemoteViewsServiceIntentFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.childrenRemoteViewsFactoryFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.childrenRemoteViewsServiceIntentFactory
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter

@Module
internal object ChildrenRemoteViewsServiceModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideChildrenRemoteViewsService(): ChildrenRemoteViewsServiceIntentFactory {
        return ::childrenRemoteViewsServiceIntentFactory
    }
}

@Module(includes = [
    TextFormatterModule::class
])
internal object ChildrenRemoteViewsFactoryModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideChildrenRemoteViewsFactory(
            formatter: Lazy<Formatter>,
            dateManager: Lazy<DateManager>
    ): ChildrenRemoteViewsFactoryFactory {
        return ::childrenRemoteViewsFactoryFactory.curried()(formatter)(dateManager).uncurried()
    }
}
