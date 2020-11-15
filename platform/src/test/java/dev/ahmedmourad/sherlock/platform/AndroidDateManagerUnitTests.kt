package dev.ahmedmourad.sherlock.platform

import android.text.format.DateUtils
import androidx.test.core.app.ApplicationProvider
import dev.ahmedmourad.sherlock.platform.managers.AndroidDateManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidDateManagerUnitTests {

    private lateinit var manager: AndroidDateManager

    @Before
    fun setup() {
        manager = AndroidDateManager()
    }

    @Test
    fun `getRelativeDateTimeString should return the correct relative date time string`() {
        listOf(System.currentTimeMillis() + 2000000,
                System.currentTimeMillis() - 2000000,
                System.currentTimeMillis() - 4000000,
                System.currentTimeMillis() - 48000000,
                System.currentTimeMillis() - 100000000,
                System.currentTimeMillis() - 550000000,
                System.currentTimeMillis() - 2000000000).map {
            (DateUtils.getRelativeDateTimeString(ApplicationProvider.getApplicationContext(),
                    it,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0).toString()
                    to manager.getRelativeDateTimeString(it))
        }.forEach { (systemValue, managerValue) ->
            assertEquals(systemValue, managerValue)
        }
    }
}
