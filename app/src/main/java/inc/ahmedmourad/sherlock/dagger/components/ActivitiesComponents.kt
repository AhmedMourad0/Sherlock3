package inc.ahmedmourad.sherlock.dagger.components

import dagger.Subcomponent
import inc.ahmedmourad.sherlock.dagger.modules.GlobalViewModelModule
import inc.ahmedmourad.sherlock.dagger.modules.MainActivityViewModelModule
import inc.ahmedmourad.sherlock.view.activity.MainActivity

@Subcomponent(modules = [
    MainActivityViewModelModule::class,
    GlobalViewModelModule::class
])
internal interface MainActivityComponent {
    fun inject(activity: MainActivity)
}
