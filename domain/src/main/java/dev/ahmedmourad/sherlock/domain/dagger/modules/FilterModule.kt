package dev.ahmedmourad.sherlock.domain.dagger.modules

import arrow.syntax.function.partially1
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenCriteriaFactory
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.ChildrenFilterFactory
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.childrenFilterFactory
import dev.ahmedmourad.sherlock.domain.dagger.modules.factories.childrenLooseCriteriaFactory
import dev.ahmedmourad.sherlock.domain.platform.LocationManager

@Module
internal object CriteriaModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(locationManager: Lazy<LocationManager>): ChildrenCriteriaFactory {
        return ::childrenLooseCriteriaFactory.partially1(locationManager)
    }
}

@Module(includes = [CriteriaModule::class])
internal object FilterModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provide(criteriaFactory: ChildrenCriteriaFactory): ChildrenFilterFactory {
        return ::childrenFilterFactory.partially1(criteriaFactory)
    }
}
