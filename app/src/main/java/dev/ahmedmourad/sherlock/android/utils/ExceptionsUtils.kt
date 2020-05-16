package dev.ahmedmourad.sherlock.android.utils

import dev.ahmedmourad.sherlock.android.R
import splitties.init.appCtx

internal fun somethingWentWrong(e: Throwable? = null): String {
    return e?.message?.let { msg ->
        appCtx.getString(R.string.something_went_wrong_with_message, msg)
    } ?: appCtx.getString(R.string.something_went_wrong)
}
