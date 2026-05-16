package com.alif.studentroutine.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class LocationHelper(private val context: Context) {

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Flow<LocationResult> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(LocationResult.PermissionDenied)
            close()
            return@callbackFlow
        }

        val manager = locationManager ?: run {
            trySend(LocationResult.Error("Location manager unavailable"))
            close()
            return@callbackFlow
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(LocationResult.Success(location.latitude, location.longitude))
                close()
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val provider = if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            LocationManager.NETWORK_PROVIDER
        } else {
            trySend(LocationResult.Error("No location provider available"))
            close()
            return@callbackFlow
        }

        manager.requestLocationUpdates(
            provider,
            0L,
            0f,
            listener,
            Looper.getMainLooper()
        )

        awaitClose {
            manager.removeUpdates(listener)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Flow<LocationResult> = flow {
        if (!hasLocationPermission()) {
            emit(LocationResult.PermissionDenied)
            return@flow
        }

        val manager = locationManager ?: run {
            emit(LocationResult.Error("Location manager unavailable"))
            return@flow
        }

        val location: Location? = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

        if (location != null) {
            emit(LocationResult.Success(location.latitude, location.longitude))
        } else {
            emit(LocationResult.NoLocation)
        }
    }
}

sealed class LocationResult {
    data class Success(val latitude: Double, val longitude: Double) : LocationResult()
    data object PermissionDenied : LocationResult()
    data object NoLocation : LocationResult()
    data class Error(val message: String) : LocationResult()
}
