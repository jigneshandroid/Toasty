package com.example.toasty

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.toasty.databinding.ActivityLearnKotlinBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

    private fun handleCoroutineException() = runBlocking {
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
}