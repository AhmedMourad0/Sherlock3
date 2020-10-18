package dev.ahmedmourad.sherlock.platform

import dev.ahmedmourad.sherlock.platform.managers.AndroidLocationManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidLocationManagerUnitTests {

    private lateinit var manager: AndroidLocationManager

    @Before
    fun setup() {
        manager = AndroidLocationManager()
    }

    @Test
    fun `distanceBetween should return the distance in meters between the two coordinates`() {
        mapOf(18504L to ((30.7895331 to 30.9902482) to (30.8254819 to 30.8014204)),
                4076L to ((30.7895331 to 30.9902482) to (30.8261675 to 30.9938675)),
                18412L to ((30.8254819 to 30.8014204) to (30.8261675 to 30.9938675))
        ).map { (systemDistance, coordinates) ->
            systemDistance to manager.distanceBetween(coordinates.first.first,
                    coordinates.first.second,
                    coordinates.second.first,
                    coordinates.second.second
            )
        }.forEach { (systemDistance, managerDistance) ->
            println("$systemDistance  -  $managerDistance")
            assertEquals(systemDistance, managerDistance)
        }
    }
}
