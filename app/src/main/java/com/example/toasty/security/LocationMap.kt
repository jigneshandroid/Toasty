package com.example.toasty.security

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object LocationMap {

    private const val TAG: String = "LocationMap"

    val locations = MutableLiveData<ArrayList<String>>()

    @SuppressLint("QueryPermissionsNeeded", "WrongConstant")
    fun showLocationOnExternalMap(activity: Activity, locations: ArrayList<String>) {
        if (locations.isEmpty()) return

        // Create a route query by concatenating the locations
        val route = locations.joinToString(separator = "/")
        val mapUri = Uri.parse("https://www.google.com/maps/dir/$route")
        //val geoUri = Uri.parse("geo:22.3196986,73.1690153")
        //val uri = Uri.parse("http://maps.google.com/maps?daddr=37.7749,-122.4194")
        // Open the map with the intent
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        //mapIntent.setPackage("com.google.android.apps.maps")
        //mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        val activityInfo = mapIntent.resolveActivityInfo(activity.packageManager, mapIntent.flags)
        activity.startActivity(mapIntent)
        if (activityInfo?.exported != null) {

        } else {
            Log.d(TAG, "No app found to handle the map intent.")
        }
    }
}