package dev.ahmedmourad.sherlock.android.dagger.modules

import arrow.syntax.function.curried
import arrow.syntax.function.uncurried
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.ChildrenRemoteViewsFactoryFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.ChildrenRemoteViewsServiceIntentFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.childrenRemoteViewsFactoryFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.childrenRemoteViewsServiceIntentFactory
import dev.ahmedmourad.sherlock.android.utils.formatter.Formatter
import dev.ahmedmourad.sherlock.domain.platform.DateManager

@Module
internal object ChildrenRemoteViewsServiceModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): ChildrenRemoteViewsServiceIntentFactory {
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
    fun provide(
            formatter: Lazy<Formatter>,
            dateManager: Lazy<DateManager>
    ): ChildrenRemoteViewsFactoryFactory {
        return ::childrenRemoteViewsFactoryFactory.curried()(formatter)(dateManager).uncurried()
    }
}
