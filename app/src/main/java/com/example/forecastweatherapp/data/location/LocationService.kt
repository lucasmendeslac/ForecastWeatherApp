package com.example.forecastweatherapp.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

interface LocationService {
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
    fun getLocationUpdates(intervalMs: Long): Flow<Location>
    suspend fun getCurrentLocation(): Location?
}

class LocationServiceImpl(
    private val context: Context
) : LocationService {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(intervalMs: Long): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            throw LocationException("Faltam permissões de localização")
        }
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    trySend(location)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            throw LocationException("Faltam permissões de localização")
        }
        
        return try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }
}

class LocationException(message: String) : Exception(message) 