package dev.ahmedmourad.sherlock.android.dagger.components

import dagger.Subcomponent
import dev.ahmedmourad.sherlock.android.dagger.modules.GlobalViewModelModule
import dev.ahmedmourad.sherlock.android.dagger.modules.MainActivityViewModelModule
import dev.ahmedmourad.sherlock.android.view.activity.MainActivity

@Subcomponent(modules = [
    MainActivityViewModelModule::class,
    GlobalViewModelModule::class
])
internal interface MainActivityComponent {
    fun inject(activity: MainActivity)
}
