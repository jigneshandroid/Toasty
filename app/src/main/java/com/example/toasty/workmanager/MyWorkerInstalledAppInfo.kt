package com.example.toasty.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.toasty.AppInfo
import com.example.toasty.security.FirebaseData.deviceInfo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyWorkerInstalledAppInfo(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object{
        const val TAG = "MyWorkerInstalledAppInfo"
    }
    override suspend fun doWork(): Result {
        // Perform your background task here.
        return try {
            // Example: Simulate a task
            val input = inputData.getString("key")
            Log.d(TAG, "doWork call $input")
            //Thread.sleep(3000) // Simulate a long-running operation
            getInstalledApps(applicationContext)
            Log.d(TAG, "doWork call success")
            Result.success()  // Indicate success
        } catch (e: Exception) {
            Log.d(TAG, "doWork call failure")
            Result.failure()  // Indicate failure
        }
    }

    @SuppressLint("QueryPermissionsNeeded", "HardwareIds")
    private fun getInstalledApps(context: Context) {
        val pm: PackageManager = context.packageManager
        val deviceUid = (deviceInfo + Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )) ?: "Android"
        // Initialize Firebase Database reference
        val databaseReference: DatabaseReference= FirebaseDatabase.getInstance().reference.child(deviceUid)
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        var counter: Int = 0
        for (packageInfo in packages) {
            val name = pm.getApplicationLabel(packageInfo).toString()
            val packageName = packageInfo.packageName
            val isSystemApp = (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val systemApp = if(isSystemApp) "SystemApp" else "NotSystemApp"
            if(!isSystemApp) {
                counter++
                databaseReference.child("InstalledApps").child(systemApp)
                    .child((counter).toString())
                    .setValue(AppInfo(name, packageName, isSystemApp))
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "InstalledApps $counter : ${AppInfo(name, packageName, isSystemApp)}"
                        )
                    }
                    .addOnFailureListener { exception ->
                        Log.d(
                            TAG,
                            "Failed to save InstalledApps: ${exception.message}"
                        )
                    }
            }
        }
    }
}
