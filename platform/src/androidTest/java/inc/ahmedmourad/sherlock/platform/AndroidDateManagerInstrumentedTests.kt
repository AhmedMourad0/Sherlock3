package inc.ahmedmourad.sherlock.platform

import android.text.format.DateUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import inc.ahmedmourad.sherlock.platform.managers.AndroidDateManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error

@RunWith(AndroidJUnit4::class)
class AndroidDateManagerInstrumentedTests {

    private lateinit var manager: AndroidDateManager

    @Before
    fun setup() {
        Timber.plant(Timber.DebugTree())
        setupManager()
    }

    private fun setupManager() {
        manager = AndroidDateManager()
    }

    @Test
    fun getRelativeDateTimeString_shouldReturnTheSameRelativeDateTimeStringAsTheSystemCall() {

        listOf(System.currentTimeMillis() + 2000000,
                System.currentTimeMillis() - 2000000,
                System.currentTimeMillis() - 2000000,
                System.currentTimeMillis() - 4000000,
                System.currentTimeMillis() - 48000000,
                System.currentTimeMillis() - 100000000,
                System.currentTimeMillis() - 550000000,
                System.currentTimeMillis() - 2000000000).map {
            (DateUtils.getRelativeDateTimeString(appCtx,
                    it,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.YEAR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_WEEKDAY).toString()
                    to
                    manager.getRelativeDateTimeString(it))
        }.forEach { (systemValue, managerValue) ->
            Timber.error("$systemValue  -  $managerValue")
            assertEquals(systemValue, managerValue)
        }
    }
}
