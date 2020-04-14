package dev.ahmedmourad.sherlock.platform

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.ahmedmourad.sherlock.platform.managers.AndroidTextManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import splitties.init.appCtx
import timber.log.Timber
import timber.log.error

@RunWith(AndroidJUnit4::class)
class AndroidTextManagerInstrumentedTests {

    private lateinit var manager: AndroidTextManager

    @Before
    fun setup() {
        Timber.plant(Timber.DebugTree())
        setupManager()
    }

    private fun setupManager() {
        manager = AndroidTextManager()
    }

    @Test
    fun allMethods_shouldReturnTheSameStringAsTheSystemCall() {

        mapOf(appCtx.getString(R.string.white_skin) to manager.whiteSkin(),
                appCtx.getString(R.string.wheatish_skin) to manager.wheatishSkin(),
                appCtx.getString(R.string.dark_skin) to manager.darkSkin(),
                appCtx.getString(R.string.blonde_hair) to manager.blondeHair(),
                appCtx.getString(R.string.brown_hair) to manager.brownHair(),
                appCtx.getString(R.string.dark_hair) to manager.darkHair(),
                appCtx.getString(R.string.male) to manager.male(),
                appCtx.getString(R.string.female) to manager.female()
        ).forEach { (systemValue, managerValue) ->
            Timber.error("$systemValue  -  $managerValue")
            assertEquals(systemValue, managerValue)
        }
    }
}
