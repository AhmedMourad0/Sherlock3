package dev.ahmedmourad.sherlock.android.dagger.modules

import arrow.syntax.function.curried
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.AppSectionsRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.ChildrenRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.appSectionsRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.childrenRecyclerAdapterFactory
import dev.ahmedmourad.sherlock.android.utils.formatter.Formatter
import dev.ahmedmourad.sherlock.domain.platform.DateManager

@Module(includes = [
    TextFormatterModule::class
])
internal object ChildrenRecyclerAdapterModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(
            dateManager: Lazy<DateManager>,
            formatter: Lazy<Formatter>
    ): ChildrenRecyclerAdapterFactory {
        return ::childrenRecyclerAdapterFactory.curried()(dateManager)(formatter)
    }
}

@Module
internal object AppSectionsRecyclerAdapterModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(): AppSectionsRecyclerAdapterFactory {
        return ::appSectionsRecyclerAdapterFactory
    }
}
