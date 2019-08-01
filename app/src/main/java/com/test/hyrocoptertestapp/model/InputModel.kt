package com.test.hyrocoptertestapp.model

import com.google.android.gms.maps.model.LatLng

interface InputData

data class InputModel (
        val gpsTime: Long,
        val lat: Long,
        val lon: Long,
        val alt: Double,
        val heading: Double,
        val speed: Double,
        val range: Double,
        val bearing: Double,
        val autopilotDir: Double,
        val angleError: Double,
        val imuYaw: Double,
        val imuPitch: Double,
        val imuRoll: Double,
        val barAlt: Double,
        val steering: Int,
        val mode: String
): InputData {
    fun getLatLng(): LatLng{
        return LatLng(lat/10000000.0, lon/10000000.0)
    }

    fun getSpeedKmH(): Double = speed*1.852

    override fun toString(): String {
        return "$gpsTime, $lat, $lon, $alt, $heading, $speed, $range, $bearing, $autopilotDir, $angleError, $imuYaw, $imuPitch, $imuRoll, $barAlt, $steering, $mode"
    }
}