package dev.ahmedmourad.sherlock.android.dagger.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.SherlockServiceIntentFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.factories.sherlockServiceIntentFactory
import dev.ahmedmourad.sherlock.android.dagger.modules.qualifiers.SherlockServiceIntentQualifier

@Module
internal object SherlockServiceModule {
    @Provides
    @Reusable
    @SherlockServiceIntentQualifier
    @JvmStatic
    fun provide(): SherlockServiceIntentFactory {
        return ::sherlockServiceIntentFactory
    }
}
