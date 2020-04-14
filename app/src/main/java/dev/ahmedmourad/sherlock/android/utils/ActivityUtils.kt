package dev.ahmedmourad.sherlock.android.utils

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import splitties.init.appCtx

fun Activity.hideSoftKeyboard() {
    this.currentFocus?.let {
        val inputMethodManager = appCtx.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
    }
}
