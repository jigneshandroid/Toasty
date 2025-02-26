package com.example.toasty

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.toasty.databinding.ActivityMainBinding
import com.jigs.chatgptdemo.network.ApiClient
import com.jigs.chatgptdemo.network.ChatRequest
import com.jigs.chatgptdemo.network.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatGpyActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        mBinding.sendButton.setOnClickListener {
            val userMessage = mBinding.userInput.text.toString()
            if (userMessage.isNotEmpty()) {
                fetchResponse(userMessage)
            }
        }
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
