package inc.ahmedmourad.sherlock.dagger.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.dagger.modules.factories.SherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.sherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.dagger.modules.qualifiers.SherlockServiceIntentQualifier

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
