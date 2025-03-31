package com.example.toasty.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.toasty.R
import com.example.toasty.tensorflow.ObjectDetectorHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import java.io.IOException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class ActivityMap : AppCompatActivity(), SensorEventListener, OnMapReadyCallback {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null
    private lateinit var mapView: MapView
    private lateinit var txtSteps: TextView
    private var googleMap: GoogleMap? = null
    private var userMarker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val accelData = FloatArray(3)
    private val gyroData = FloatArray(3)
    private val magnetData = FloatArray(3)
    private var heading = 0f
    private var stepCount = 0
    private val isStepDetected = false
    private var lastStepTime: Long = 0
    private var userLat = 37.7749
    private var userLng = -122.4194 // Example starting location (San Francisco)
    private var printString: String = "Step count"
    private val routePoints: ArrayList<LatLng> = ArrayList()
    private var polyline: Polyline? = null
    private lateinit var objectDetectorHelper: ObjectDetectorHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        txtSteps = findViewById<TextView>(R.id.txtSteps)
        mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer != null) sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        if (gyroscope != null) sensorManager?.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_UI);

        if (magnetometer != null) sensorManager?.registerListener(
            this,
            magnetometer,
            SensorManager.SENSOR_DELAY_UI
        )

        detectTensorFlowObject()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val startLocation = LatLng(userLat, userLng)
        println("FetchMap onMapReady: $startLocation")
        //printString = "onMapReady: $startLocation"
        //txtSteps.text = printString +"...Steps:"+ stepCount + "...Heading:"+ heading
        userMarker = googleMap!!.addMarker(MarkerOptions().position(startLocation).title("User"))
        googleMap!!.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                startLocation,
                19f
            )
        ) // Zoom into indoor area
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelData, 0, event.values.size)
            detectStep(event.values[1])
        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroData, 0, event.values.size)
            //calculateHeading()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetData, 0, event.values.size)
        }
        txtSteps.text = "Accel: \n X=" + accelData[0] + "\n Y=" + accelData[1] + "\n Z=" + accelData[2] + "\n" +
                "Gyro:\n X=" + gyroData[0] + "\n Y=" + gyroData[1] + "\n Z=" + gyroData[2] + "\n" +
                "Magnet:\n X=" + magnetData[0] + "\n Y=" + magnetData[1] + "\n Z=" + magnetData[2] + "\n" +
                "Steps: " + stepCount + "\n" + "Heading: " + heading + "°\n"
        calculateHeading()
    }

    private fun detectStep(accelY: Float) {
        val currentTime = System.currentTimeMillis()
        //println("FetchMap detectStep: ${abs(accelY.toDouble())}")
        //stepCount = abs(accelY.toDouble()).toInt()
        //txtSteps.text = printString +"...Steps:"+ stepCount + "...Heading:"+ heading
        if (abs(accelY.toDouble()) > 11 && (currentTime - lastStepTime > 500)) { // Simple peak detection for step counting
            stepCount++
            lastStepTime = currentTime
            updatePosition()
        }
    }

    private fun calculateHeading() {
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        heading = Math.toDegrees(orientationAngles[0].toDouble()).toFloat() // Azimuth
        println("FetchMap Steps: $stepCount  Heading: $heading °")
        //txtSteps.text =  "Steps: " + stepCount + "\n" + "Heading: " + heading + "°\n"
    }

    private fun updatePosition() {
        val deltaX =
            STEP_LENGTH * cos(Math.toRadians(heading.toDouble())) / 111000 // Convert meters to latitude
        val deltaY = STEP_LENGTH * sin(Math.toRadians(heading.toDouble())) / (111000 * cos(
            Math.toRadians(userLat)
        )) // Convert meters to longitude

        userLat += deltaX
        userLng += deltaY
        //txtSteps.text =  "userLat: " + userLat + "\n" + "userLng: " + userLng + "\n"

        if (googleMap != null && userMarker != null) {
            val newPosition = LatLng(userLat, userLng)
            userMarker!!.position = newPosition
            //googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(newPosition))
            //txtSteps.text =  "$newPosition...Steps:"+ stepCount + "...Heading:"+ heading
            addMarkerAndDrawLine(newPosition)
        }
    }

    // Function to add a marker and draw a polyline
    private fun addMarkerAndDrawLine(latLng: LatLng) {
        // Add marker
        //googleMap?.addMarker(MarkerOptions().position(latLng).title("Marker at $latLng"))

        // Add point to the list
        routePoints.add(latLng)

        // Remove old polyline and draw a new one
        polyline?.remove()
        polyline = googleMap?.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .width(8f)
                .color(-0xffff01) // Blue Color
        )

        // Move camera to latest marker
        //googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager!!.unregisterListener(this)
        mapView!!.onDestroy()
    }

    companion object {
        private const val STEP_LENGTH = 0.7f // Approximate step length in meters
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Log.e("MainActivity", "Permission denied")
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationTask: Task<Location> = fusedLocationClient.lastLocation

            locationTask.addOnSuccessListener { location ->
                if (location != null) {
                    userLat = location.latitude
                    userLng = location.longitude
                    updatePosition()
                    mapView.getMapAsync(this)
                    Log.d("MainActivity", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                } else {
                    Log.e("MainActivity", "Location is null")
                }
            }.addOnFailureListener { e ->
                Log.e("MainActivity", "Error getting location", e)
            }
        }
    }

    private fun detectObjects(bitmap: Bitmap) {
        val detections = objectDetectorHelper.detectObjects(bitmap)

        for (detection in detections) {
            val category = detection.categories.firstOrNull()
            val label = category?.label ?: "Unknown"
            val score = category?.score ?: 0.0f

            Log.d("ObjectDetection", "Detected: $label, Confidence: $score")
        }
    }

    private fun detectTensorFlowObject(){
        objectDetectorHelper = ObjectDetectorHelper(this)

        // Load image from assets (or use Camera/Storage)
        val bitmap = loadBitmapFromAssets("test_image.jpg")
        if (bitmap != null) {
            detectObjects(bitmap)
        }
    }

    private fun loadBitmapFromAssets(fileName: String): Bitmap? {
        return try {
            val inputStream = assets.open(fileName)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
