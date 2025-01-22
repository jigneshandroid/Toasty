package com.example.toasty

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.toasty.databinding.ActivityLearnKotlinBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class ActivityLearnKotlin : AppCompatActivity() {
    private lateinit var mBinding: ActivityLearnKotlinBinding
    private val TAG: String = "ActivityLearnKotlinTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_learn_kotlin)
        handleCoroutineException()
    }

    private fun handleCoroutineException() {
        //handleCoroutineExceptionTryCatch()
        //handleCoroutineExceptionHandler()
        handleCoroutineExceptionsupervisorScope()
    }

    private fun handleCoroutineExceptionTryCatch() = runBlocking {
        try {
            val result = withContext(Dispatchers.IO) {
                // Simulate an exception
                throw IllegalArgumentException("Something went wrong!")
            }
            Log.d(TAG,"Result: $result")
        } catch (e: Exception) {
            Log.d(TAG,"Caught exception: ${e.message}")
        }
    }

    private fun handleCoroutineExceptionHandler() = runBlocking {
        // Define a CoroutineExceptionHandler
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Log.d(TAG,"Caught exception in CoroutineExceptionHandler: ${exception.message}")
        }

        val job = launch(exceptionHandler) {
            // Simulate an exception
            throw ArithmeticException("An error occurred!")
        }
        job.join()
    }

    private fun handleCoroutineExceptionsupervisorScope() = runBlocking {
        supervisorScope {
            val job1 = launch {
                Log.d(TAG,"Job1 started")
                throw RuntimeException("Job1 failed!")
            }

            val job2 = launch {
                Log.d(TAG,"Job2 started")
                delay(1000)
                Log.d(TAG,"Job2 completed")
            }

            job1.join()
            job2.join()
        }
        Log.d(TAG,"SupervisorScope completed")
    }

}