package dev.ahmedmourad.sherlock.platform

import dev.ahmedmourad.sherlock.platform.managers.AndroidTextManager
import inc.ahmedmourad.sherlock.platform.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import splitties.init.appCtx

@RunWith(RobolectricTestRunner::class)
class AndroidTextManagerUnitTests {

    private lateinit var manager: AndroidTextManager

    @Before
    fun setup() {
        manager = AndroidTextManager()
    }

    @Test
    fun `allMethods should return the same string as the system call`() {
        mapOf(appCtx.getString(R.string.white_skin) to manager.whiteSkin(),
                appCtx.getString(R.string.wheatish_skin) to manager.wheatishSkin(),
                appCtx.getString(R.string.dark_skin) to manager.darkSkin(),
                appCtx.getString(R.string.blonde_hair) to manager.blondeHair(),
                appCtx.getString(R.string.brown_hair) to manager.brownHair(),
                appCtx.getString(R.string.dark_hair) to manager.darkHair(),
                appCtx.getString(R.string.male) to manager.male(),
                appCtx.getString(R.string.female) to manager.female()
        ).forEach { (systemValue, managerValue) ->
            assertEquals(systemValue, managerValue)
        }
    }
}
