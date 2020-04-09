package inc.ahmedmourad.sherlock.dagger.modules

import arrow.syntax.function.curried
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.dagger.modules.factories.AppSectionsRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.ChildrenRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.appSectionsRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.childrenRecyclerAdapterFactory
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.utils.formatter.Formatter

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
