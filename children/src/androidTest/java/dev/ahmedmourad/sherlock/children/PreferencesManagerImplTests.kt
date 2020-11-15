package dev.ahmedmourad.sherlock.children

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import dev.ahmedmourad.sherlock.children.preferences.PreferencesManagerImpl
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class PreferencesManagerImplTests {

    private lateinit var manager: PreferencesManagerImpl

    @Before
    fun setup() {
        manager = PreferencesManagerImpl()
    }

    @Test
    fun getDeviceId_shouldNeverReturnBlankString() {
        repeat((1..5).random()) {
            assertTrue(manager.getDeviceId().isNotBlank())
        }
    }

    @Test
    fun getDeviceId_shouldReturnDifferentIdForDifferentDevices() {

        val id = manager.getDeviceId()
        assertTrue(id.isNotBlank())
        manager.simulateNewDevice()

        val newId = manager.getDeviceId()
        assertTrue(newId.isNotBlank())
        assertNotEquals(id, newId)
    }
}
