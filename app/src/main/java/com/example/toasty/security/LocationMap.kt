package com.example.toasty.security

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object LocationMap {


        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG: String = "LocationMap"
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var locationRequest: LocationRequest
        private lateinit var locationCallback: LocationCallback
        private var lastLatLong: String? = null

        fun onInit(activity: Activity) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

            // Create location request
            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(3000L) // Minimum interval for updates
                .build()

            // Create location callback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    if (location != null) {
                        // Handle the location update
                        Log.d(TAG, "Location: ${location.latitude}, ${location.longitude}")
                        val currentLatLong = "${location.latitude},${location.longitude}"
                        if (currentLatLong != lastLatLong) {
                            lastLatLong = currentLatLong
                            FirebaseData.saveLocationToDatabase(currentLatLong)
                        }
                    }
                }
            }

            fetchLocation(activity)
        }

        fun removeLocationUpdate() {
            // Stop location updates to avoid memory leaks
            if(::fusedLocationClient.isInitialized) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

        private fun fetchLocation(activity: Activity) {
            // Check permissions and fetch location
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                /*              fusedLocationClient.lastLocation
                                  .addOnSuccessListener { location ->
                                      if (location != null) {
                                          val latitude = location.latitude
                                          val longitude = location.longitude
                                          FirebaseData.saveLocationToDatabase(latitude.toString(), longitude.toString())
                                          *//*Toast.makeText(
                                activity,
                                "Location: ($latitude, $longitude)",
                                Toast.LENGTH_LONG
                            ).show()*//*
                        } else {
                            Toast.makeText(activity, "Unable to fetch location.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, "Failed to fetch location: ${it.message}", Toast.LENGTH_SHORT).show()
                    }*/
            } else {
                // Request location permissions
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }

}