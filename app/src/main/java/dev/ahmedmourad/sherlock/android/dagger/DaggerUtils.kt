package dev.ahmedmourad.sherlock.android.dagger

import android.content.Context
import dev.ahmedmourad.sherlock.android.application.SherlockApplication

internal fun Context.findAppComponent() = (this.applicationContext as SherlockApplication).appComponent
