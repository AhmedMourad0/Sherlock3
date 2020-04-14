package dev.ahmedmourad.sherlock.platform.managers

import android.location.Location
import dev.ahmedmourad.sherlock.domain.platform.LocationManager

internal class AndroidLocationManager : LocationManager {
    override fun distanceBetween(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double): Long {

        val distance = FloatArray(1)

        Location.distanceBetween(startLatitude,
                startLongitude,
                endLatitude,
                endLongitude,
                distance
        )

        return distance[0].toLong()
    }
}
