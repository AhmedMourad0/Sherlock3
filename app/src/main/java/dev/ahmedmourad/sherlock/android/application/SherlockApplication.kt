package dev.ahmedmourad.sherlock.android.application

import androidx.multidex.MultiDexApplication
import dev.ahmedmourad.sherlock.android.dagger.DaggerComponentProvider
import dev.ahmedmourad.sherlock.android.dagger.components.ApplicationComponent
import dev.ahmedmourad.sherlock.android.dagger.components.DaggerApplicationComponent
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

/*
*
* Work on the ui (the 3 retry options too, the (AddChildController, DisplayChildController, SearchResultsController) progress, no items in lists, place picker, maybe separate it into separate object as well), coordinator layout for the connection indicator
*
* Use Timber to its full potential (Crashlytics)
*
* Dagger encapsulation with qualifiers
*
* * Package per feature
*
* replace exceptions with ADTs in interactors
*
* replace RxJava and Coroutines Flow with Monads and Functors for more abstraction
*
* The Dagger refactoring stage (Qualifier, Binding, SingleComponent, providers, Kotlin initiative)
*
* testing
*
*  / ** Done ** /
*
* ConstraintLayout
*
* Kotlin Flows and Coroutines
*
* Kotlin Multiplatform
*
* */
