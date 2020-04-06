package inc.ahmedmourad.sherlock.dagger

import android.content.Context
import inc.ahmedmourad.sherlock.application.SherlockApplication

internal fun Context.findAppComponent() = (this.applicationContext as SherlockApplication).appComponent
