package dev.ahmedmourad.sherlock.platform.managers

import android.text.format.DateUtils
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import splitties.init.appCtx

internal class AndroidDateManager : DateManager {
    override fun getRelativeDateTimeString(
            timeMillis: Long,
            minResolution: Long,
            transitionResolution: Long
    ): String {
        return DateUtils.getRelativeDateTimeString(appCtx,
                timeMillis,
                minResolution,
                transitionResolution,
                DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_WEEKDAY
        ).toString()
    }
}
