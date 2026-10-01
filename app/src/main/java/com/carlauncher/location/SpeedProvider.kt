package com.carlauncher.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GpsData(
    val speedKmh: Float = 0f,
    val speedMph: Float = 0f,
    val headingDegrees: Float = 0f,
    val hasFix: Boolean = false,
    val satellites: Int = 0,
)

/**
 * High-accuracy GPS speed and heading provider based on the system
 * LocationManager (GPS provider only - the launcher never needs network
 * location for speed).
 */
class SpeedProvider(private val context: Context) {

    private val _gps = MutableStateFlow(GpsData())
    val gps: StateFlow<GpsData> = _gps

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var running = false

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) = update(location)
        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) {
            _gps.value = _gps.value.copy(hasFix = false)
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener,
            )
            running = true
        } catch (_: SecurityException) {
            // Permission revoked between check and request.
        } catch (_: IllegalArgumentException) {
            // No GPS provider on this device.
        }
    }

    fun stop() {
        if (!running) return
        locationManager.removeUpdates(locationListener)
        running = false
    }

    private fun update(location: Location) {
        val kmh = location.speed * 3.6f
        _gps.value = GpsData(
            speedKmh = kmh,
            speedMph = kmh * 0.6213712f,
            headingDegrees = if (location.hasBearing()) location.bearing else _gps.value.headingDegrees,
            hasFix = location.accuracy > 0f,
            satellites = location.extras?.getInt("satellites", 0) ?: 0,
        )
    }
}
