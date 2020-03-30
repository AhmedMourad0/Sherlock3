package inc.ahmedmourad.sherlock.application

import androidx.multidex.MultiDexApplication
import timber.log.LogcatTree
import timber.log.Timber

@Suppress("unused")
internal class SherlockApplication : MultiDexApplication() {
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
* SavedStateViewModel and DataBinding
*
* Dagger encapsulation with qualifiers
*
* * Package per feature
*
* replace exceptions with ADTs
*
* replace RxJava and Coroutines Flow with Monads and Functors for more abstraction
*
* The Fragments refactoring stage (Fragments, Navigation, DataBinding)
*
* The Dagger refactoring stage (Qualifier, Binding, SingleComponent, Kotlin initiative)
*
* Have a view model shared across the activity and all fragments that holds shared data such as signed in user and internet connectivity .. etc
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
