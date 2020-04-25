package dev.ahmedmourad.sherlock.android.di

import android.content.Context
import splitties.init.appCtx

internal interface DaggerComponentProvider {
    val component: ApplicationComponent
}

internal val Context.injector get() = (this.applicationContext as DaggerComponentProvider).component

internal val injector get() = appCtx.injector
