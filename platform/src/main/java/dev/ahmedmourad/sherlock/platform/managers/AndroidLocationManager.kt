package dev.ahmedmourad.sherlock.platform.managers

import android.location.Location
import dagger.Reusable
import dev.ahmedmourad.sherlock.domain.platform.LocationManager
import javax.inject.Inject

@Reusable
internal class AndroidLocationManager @Inject constructor() : LocationManager {
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
