package com.example.toasty.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object{
        const val TAG = "MyWorkerManager"
    }
    override suspend fun doWork(): Result {
        // Perform your background task here.
        return try {
            // Example: Simulate a task
            val input = inputData.getString("key")
            Log.d(TAG, "doWork call $input")
            Thread.sleep(3000) // Simulate a long-running operation
            Log.d(TAG, "doWork call success")
            Result.success()  // Indicate success
        } catch (e: Exception) {
            Log.d(TAG, "doWork call failure")
            Result.failure()  // Indicate failure
        }
    }
}
