package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.dagger.modules.HomeControllerModule
import inc.ahmedmourad.sherlock.dagger.modules.MainActivityViewModelModule
import inc.ahmedmourad.sherlock.dagger.modules.SignInControllerModule
import inc.ahmedmourad.sherlock.dagger.modules.SignedInUserProfileControllerModule
import inc.ahmedmourad.sherlock.view.activity.MainActivity

@Subcomponent(modules = [
    HomeControllerModule::class,
    MainActivityViewModelModule::class,
    SignInControllerModule::class,
    SignedInUserProfileControllerModule::class
])
internal interface MainActivityComponent {
    fun inject(activity: MainActivity)
}
