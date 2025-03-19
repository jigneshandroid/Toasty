package com.example.toasty

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.toasty.databinding.ActivityMainBinding
import com.example.toasty.models.Video
import com.example.toasty.viewmodel.FlowViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatGpyActivity : AppCompatActivity() {

    private val TAG = "ChatSpyActivity"
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(this@ChatGpyActivity, R.layout.activity_main)

        mBinding.sendButton.setOnClickListener {
            val userMessage = mBinding.userInput.text.toString()
            if (userMessage.isNotEmpty()) {
                fetchResponse(userMessage)
            }
        }

        val listVideo = listOf(
            Video.Programming("KotlinProgramming", "10:30"),
            Video.Cooking("KotlinCooking", "10:30"),
            Video.Travel("KotlinTravel", "10:30"),
            Video.Programming("KotlinProgramming2", "10:30"),
            Video.Cooking("KotlinCooking2", "10:30"),
            Video.Travel("KotlinTravel2", "10:30"),
        )

        val listProg = filter<Video.Programming>(listVideo)
        Log.d("FilterProg", listProg.toString())
    }

    private inline fun <reified T> filter(listVideo: List<Video>): List<T>{
        return listVideo.filterIsInstance<T>()
    }

    private suspend fun delayMethod(time: Long) {
        delay(time)
    }

    @SuppressLint("SetTextI18n")
    private fun fetchResponse(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                /*             val response = ApiClient.chatApi.sendMessage(
                                 ChatRequest(
                                     messages = listOf(Message("user", message))
                                 )
                             )
                             val aiResponse = response.choices.first().message.content

                             runOnUiThread {
                                 mBinding.responseText.text = aiResponse
                             }*/
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    mBinding.responseText.text = "Error: ${e.message}"
                }
            }
        }
    }
}
