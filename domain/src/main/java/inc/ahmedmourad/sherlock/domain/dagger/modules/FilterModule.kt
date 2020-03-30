package inc.ahmedmourad.sherlock.domain.dagger.modules

import arrow.syntax.function.partially1
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenCriteriaFactory
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.childrenFilterFactory
import inc.ahmedmourad.sherlock.domain.dagger.modules.factories.childrenLooseCriteriaFactory
import inc.ahmedmourad.sherlock.domain.platform.LocationManager

@Module
internal object CriteriaModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideCriteria(locationManager: Lazy<LocationManager>): ChildrenCriteriaFactory {
        return ::childrenLooseCriteriaFactory.partially1(locationManager)
    }
}

@Module(includes = [CriteriaModule::class])
internal object FilterModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideFilter(criteriaFactory: ChildrenCriteriaFactory): ChildrenFilterFactory {
        return ::childrenFilterFactory.partially1(criteriaFactory)
    }
}
