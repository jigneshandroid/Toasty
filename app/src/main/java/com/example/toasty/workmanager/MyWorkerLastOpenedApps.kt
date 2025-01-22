package com.example.toasty.workmanager

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.toasty.common.CommonUtils
import com.google.gson.Gson

class MyWorkerLastOpenedApps(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object{
        const val TAG = "MyWorkerLastOpenedApps"
    }
    override suspend fun doWork(): Result {
        // Perform your background task here.
        return try {
            // Example: Simulate a task
            val usageStatsList = getLastOpenedApp(applicationContext)// Simulate a long-running operation

            if(usageStatsList!=null) {
                val gson = Gson()
                val json = gson.toJson(usageStatsList)
                // Prepare the output data
                val outputData = Data.Builder()
                    .putString("resultData", json)
                    .putInt("resultCode", 200)
                    .build()
                Log.d(TAG, "doWork call success")
                Result.success(outputData)  // Indicate success
            }else{
                val outputData = Data.Builder()
                    .putString("resultData", null)
                    .putInt("resultCode", 0)
                    .build()
                Log.d(TAG, "doWork call success")
                Result.success(outputData)
            }
        } catch (e: Exception) {
            Log.d(TAG, "doWork call failure")
            Result.failure()  // Indicate failure
        }
    }

    private fun getLastOpenedApp(context: Context): ArrayList<Triple<String?, String?, String?>>? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Get current time and the interval for querying usage stats
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - 1000 * 60 * 60 * 24 // Query for the past hour

        // Query usage stats
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, currentTime
        )

        return if (usageStatsList.isNullOrEmpty()) {
            redirectToUsageAccessSettings(context)
            Log.d(TAG, "Usage stats permission not granted or no data available.")
            null
        } else {
            //callWorkManagerInstalledApp(context)
            // Find the last used app
            findLastOpenedApps(usageStatsList)
        }
    }

    // Redirect the user to settings to grant the required permission
    private fun redirectToUsageAccessSettings(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    private fun findLastOpenedApps(usageStatsList: List<UsageStats>): ArrayList<Triple<String?, String?, String?>>{
        // Find the last used app
        val lastUsedApp: ArrayList<Triple<String?, String?, String?>> = ArrayList<Triple<String?, String?, String?>>()
        for (usageStats in usageStatsList) {
            if (usageStats.lastTimeUsed > 0) {
                val lastTimeUsed = "lastTimeUsed= ${
                    usageStats?.lastTimeUsed?.let {
                        CommonUtils.convertTimestampToDate(it)
                    }
                }, lastTimeVisible= ${
                    usageStats?.lastTimeVisible?.let {
                        CommonUtils.convertTimestampToDate(
                            it
                        )
                    }
                }"
                val totalTimeForeground = if (usageStats?.totalTimeInForeground?.div(60000)!! > 0) {
                    "${usageStats?.totalTimeInForeground?.div(60000)} min"
                }else {
                    // Time in foreground sec
                    "${usageStats?.totalTimeInForeground?.div(1000)} sec"
                }
                lastUsedApp.add(
                    Triple(usageStats?.packageName.toString().replace('.', '_'), totalTimeForeground, lastTimeUsed)
                )
                //Log.d(TAG, "Last opened app: ${lastUsedApp.last()}")
     /*           databaseReference.child("lastOpenedApp")
                    .child(usageStats?.packageName.toString())
                if (usageStats?.totalTimeInForeground?.div(60000)!! > 0) {
                    // Time in foreground min
                    databaseReference.child("lastOpenedApp")
                        .child(usageStats?.packageName.toString().replace('.', '_'))
                        .child("totalTimeInForeground")
                        .setValue("${usageStats?.totalTimeInForeground?.div(60000)} min")
                        .addOnSuccessListener {
                            Log.d(
                                FirebaseDataService.TAG,
                                "totalTimeInForeground ${usageStats?.totalTimeInForeground ?: 0}"
                            )
                        }
                        .addOnFailureListener { exception ->
                            Log.d(FirebaseDataService.TAG, "Failed to save totalTimeInForeground: ${exception.message}")
                        }
                } else {
                    // Time in foreground sec
                    databaseReference.child("lastOpenedApp")
                        .child(usageStats?.packageName.toString().replace('.', '_'))
                        .child("totalTimeInForeground")
                        .setValue("${usageStats?.totalTimeInForeground?.div(1000)} sec")
                        .addOnSuccessListener {
                            Log.d(
                                FirebaseDataService.TAG,
                                "Time in foreground ${usageStats?.totalTimeInForeground ?: 0}"
                            )
                        }
                        .addOnFailureListener { exception ->
                            Log.d(
                                FirebaseDataService.TAG,
                                "Failed to save Time in foreground: ${exception.message}"
                            )
                        }
                }

                databaseReference.child("lastOpenedApp")
                    .child(usageStats?.packageName.toString().replace('.', '_'))
                    .child("lastTimeUsed")
                    .setValue(lastTimeUsed)
                    .addOnSuccessListener {
                        Log.d(FirebaseDataService.TAG, "Location saved to Realtime Database $lastTimeUsed")
                    }
                    .addOnFailureListener { exception ->
                        Log.d(FirebaseDataService.TAG, "Failed to save location: ${exception.message}")
                    }*/
            }
        }
        return lastUsedApp
    }
}
