package com.example.toasty.services

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.toasty.R
import com.example.toasty.security.FirebaseData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationMapService: Service() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var lastLatLong: String? = null


    override fun onCreate() {
        super.onCreate()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create location request
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(1500L) // Minimum interval for updates
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

        Log.d(TAG, "onCreate call")
        // Start fetching location
        startForegroundService()
        fetchLocation()
    }

    private fun startForegroundService() {
        val channelId = "location_service_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification for the foreground service
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Fetching location in the background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Start the foreground service with the notification
        startForeground(1, notification)
        Log.d(TAG, "startForegroundService call")
    }

 /*   private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Handle location update here
                    // Example: Log location or send it to your server
                    Log.d(TAG,"Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }
*/
    private fun fetchLocation() {
        // Check permissions and fetch location
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "fetchLocation call")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun removeLocationUpdate() {
        // Stop location updates to avoid memory leaks
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up when service is destroyed
        Log.d(TAG, "onDestroy call")
        removeLocationUpdate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private var activity: Activity? = null
        fun startLocationService(activity: Activity) {
            val serviceIntent = Intent(activity, LocationMapService::class.java)
            ContextCompat.startForegroundService(activity, serviceIntent)
            Companion.activity = activity
        }

        fun stopLocationService(activity: Activity) {
            val serviceIntent = Intent(activity, LocationMapService::class.java)
            activity.stopService(serviceIntent)
            Log.d(TAG, "LocationMapService Stopped")
        }

        private const val TAG: String = "LocationMapService"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}