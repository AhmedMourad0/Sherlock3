package dev.ahmedmourad.sherlock.domain.platform

interface DateManager {

    fun getRelativeDateTimeString(
            timeMillis: Long,
            minResolution: Long = MINUTE_IN_MILLIS,
            transitionResolution: Long = WEEK_IN_MILLIS
    ): String

    companion object {
        private const val SECOND_IN_MILLIS = 1000L
        const val MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60L
        private const val HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60L
        private const val DAY_IN_MILLIS = HOUR_IN_MILLIS * 24L
        private const val WEEK_IN_MILLIS = DAY_IN_MILLIS * 7L
        const val YEAR_IN_MILLIS = WEEK_IN_MILLIS * 52L
    }
}
