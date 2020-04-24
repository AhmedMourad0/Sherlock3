package dev.ahmedmourad.sherlock.android.dagger

import android.content.Context
import dev.ahmedmourad.sherlock.android.dagger.components.ApplicationComponent
import splitties.init.appCtx

internal interface DaggerComponentProvider {
    val component: ApplicationComponent
}

internal val Context.injector get() = (this.applicationContext as DaggerComponentProvider).component

internal val injector get() = appCtx.injector
