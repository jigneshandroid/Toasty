package com.example.toasty.security

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationMap {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private lateinit var fusedLocationClient: FusedLocationProviderClient

        fun onInit(activity: Activity) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

            fetchLocation(activity)
        }

        private fun fetchLocation(activity: Activity) {
            // Check permissions and fetch location
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            Toast.makeText(
                                activity,
                                "Location: ($latitude, $longitude)",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(activity, "Unable to fetch location.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, "Failed to fetch location: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
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
}