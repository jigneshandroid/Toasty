package com.example.toasty.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.toasty.R
import com.example.toasty.databinding.ActivityLearnKotlinBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class ActivityLearnKotlin : AppCompatActivity() {
    private lateinit var mBinding: ActivityLearnKotlinBinding
    private val TAG: String = "ActivityLearnKotlinTest"
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "ToastyDataStore")
    private val _stateFlow = MutableStateFlow("Initial State")
    val stateFlow: StateFlow<String> = _stateFlow
    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }

    fun updateState(newValue: String) {
        _stateFlow.value = newValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(this@ActivityLearnKotlin,
            R.layout.activity_learn_kotlin
        )
        //handleCoroutineException()
        CoroutineScope(Dispatchers.IO).launch {
            fetchNumbers().collect { value ->
                println("FlowReceived: $value")
            }
            fetchNumbers()
                .map { it * 2 }
                .filter { it > 5 }
                .collect { value ->
                println("FlowReceived1: $value ${Thread.currentThread().name}")
            }
            fetchData().flowOn(Dispatchers.IO).collectLatest { value ->
                println("FlowReceived Processing $value")
                delay(1000) // Cancels if a new value arrives
            }
        }
        lifecycleScope.launch {
            fetchNumbers().collect { value ->
                println("FlowReceived2: $value ${Thread.currentThread().name}")
            }
            stateFlow.collect { value ->
                println("FlowReceived: $value ${Thread.currentThread().name}")
            }
        }
        lifecycleScope.launch {
            fetchNumbers().collect { value ->
                println("FlowReceived3: $value ${Thread.currentThread().name}")
            }
        }
    }

    private fun fetchData(): Flow<String> = flow {
        emit("Loading...")
        delay(2000)
        emit("Data Fetched!")
        throw Exception("Network Error")
    }.catch { e ->
        emit("Error: ${e.message}")
    }

    private fun fetchNumbers(): Flow<Int> = flow {
        for (i in 1..5) {
            delay(1000) // Simulating delay
            emit(i) // Emitting values
        }
    }.flowOn(Dispatchers.Main)

    private suspend fun initDataStore(){

        // Get username
        val username: Flow<String?> = dataStore.data.map { preferences ->
            preferences[USERNAME_KEY] ?: "Guest"
        }

        // Get login status
        val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }

        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username.toString()
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn as Boolean
        }


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
            Log.d(TAG, "Result: $result")
        } catch (e: Exception) {
            Log.d(TAG, "Caught exception: ${e.message}")
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.d(TAG, "Caught exception in CoroutineExceptionHandler: ${exception.message}")
    }

    private fun handleCoroutineExceptionHandler() = runBlocking {
        // Define a CoroutineExceptionHandler


        val job = launch(exceptionHandler) {
            // Simulate an exception
            throw ArithmeticException("An error occurred!")
        }
        job.join()
    }

    private fun handleCoroutineExceptionsupervisorScope() = runBlocking {

        supervisorScope {
            val job1 = launch(exceptionHandler) {
                Log.d(TAG, "Job1 started")
                throw RuntimeException("Job1 failed!")
            }

            val job2 = launch {
                Log.d(TAG, "Job2 started")
                delay(1000)
                Log.d(TAG, "Job2 completed")
            }

            //job1.join()
            //job2.join()
        }
        Log.d(TAG, "SupervisorScope completed")
    }

}