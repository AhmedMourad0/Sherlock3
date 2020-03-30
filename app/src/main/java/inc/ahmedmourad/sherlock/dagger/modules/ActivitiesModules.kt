package inc.ahmedmourad.sherlock.dagger.modules

import dagger.Module
import dagger.Provides
import dagger.Reusable
import inc.ahmedmourad.sherlock.dagger.modules.factories.MainActivityIntentFactory
import inc.ahmedmourad.sherlock.dagger.modules.factories.mainActivityIntentFactory

@Module
internal object MainActivityModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideMainActivity(): MainActivityIntentFactory {
        return ::mainActivityIntentFactory
    }
}
