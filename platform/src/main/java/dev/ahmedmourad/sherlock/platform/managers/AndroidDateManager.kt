package dev.ahmedmourad.sherlock.platform.managers

import android.text.format.DateUtils
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.DateManager
import splitties.init.appCtx
import javax.inject.Inject

@Reusable
internal class AndroidDateManager @Inject constructor() : DateManager {
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
