package dev.ahmedmourad.sherlock.domain.platform

interface LocationManager {
    fun distanceBetween(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double): Long
}
