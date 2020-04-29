package dev.ahmedmourad.sherlock.android.application

import androidx.multidex.MultiDexApplication
import dev.ahmedmourad.sherlock.android.di.ApplicationComponent
import dev.ahmedmourad.sherlock.android.di.DaggerApplicationComponent
import dev.ahmedmourad.sherlock.android.di.DaggerComponentProvider
import timber.log.LogcatTree
import timber.log.Timber

@Suppress("unused")
internal class SherlockApplication : MultiDexApplication(), DaggerComponentProvider {
    override val component: ApplicationComponent = DaggerApplicationComponent.create()
    override fun onCreate() {
        super.onCreate()
        Timber.plant(LogcatTree("Sherlock"))
    }
}
