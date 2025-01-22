package com.example.toasty.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.toasty.AppInfo
import com.google.gson.Gson

class MyWorkerInstalledAppInfo(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "MyWorkerInstalledAppInfo"
    }

    override suspend fun doWork(): Result {
        // Perform your background task here.
        return try {
            val packagesInfoList = getInstalledApps(applicationContext)
            Log.d(TAG, "doWork call success ${packagesInfoList.size}")
            if (packagesInfoList.isNotEmpty()) {
                val gson = Gson()
                val json = gson.toJson(packagesInfoList)
                val outputData = Data.Builder()
                    .putString("resultData", json)
                    .putInt("resultCode", 200)
                    .build()
                Result.success(outputData) // Indicate success
            } else {
                val outputData = Data.Builder()
                    .putString("resultData", null)
                    .putInt("resultCode", 0)
                    .build()
                Result.success(outputData) // Indicate success
            }
        } catch (e: Exception) {
            Log.d(TAG, "doWork call failure $e")
            Result.failure()  // Indicate failure
        }
    }

    @SuppressLint("QueryPermissionsNeeded", "HardwareIds")
    private fun getInstalledApps(context: Context): ArrayList<AppInfo> {
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appInfoList= ArrayList<AppInfo>()
        for (packageInfo in packages) {
            val name = pm.getApplicationLabel(packageInfo).toString()
            val packageName = packageInfo.packageName
            val isSystemApp = (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            //val systemApp = if(isSystemApp) "SystemApp" else "NotSystemApp"
            if(!isSystemApp) {
                appInfoList.add(AppInfo(name, packageName))
            }
        }
        return appInfoList
    }
}
