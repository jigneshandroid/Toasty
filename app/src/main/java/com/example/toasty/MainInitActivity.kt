package com.example.toasty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toasty.common.callActivityArCore
import com.example.toasty.common.callActivityChatSpy
import com.example.toasty.common.callActivityComposeTest
import com.example.toasty.common.callActivityLearnKotlin
import com.example.toasty.common.callActivityMapSensor
import com.example.toasty.common.callActivityRemoteMediator
import com.example.toasty.common.callActivityTensorFlowLite

class MainInitActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    @Preview(showSystemUi = true, showBackground = true)
    @Composable
    fun App() {
        Column(Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Button("TensorFlow Lite"){
                callActivityTensorFlowLite(this@MainInitActivity)
            }
            Button("ARCore"){
                callActivityArCore(this@MainInitActivity)
            }
            Button("FireBase Real DB"){
                callActivityChatSpy(this@MainInitActivity)
            }
            Button("Learn Kotlin"){
                callActivityLearnKotlin(this@MainInitActivity)
            }
            Button("Remote Mediator"){
                callActivityRemoteMediator(this@MainInitActivity)
            }
            Button("Compose Test"){
                callActivityComposeTest(this@MainInitActivity)
            }
            Button("Chat Gpy"){
                callActivityChatSpy(this@MainInitActivity)
            }
            Button("Map Sensor"){
                callActivityMapSensor(this@MainInitActivity)
            }
        }

    }

    @Composable
    fun Button(text: String, callActivity:()->Unit= { finish() }) {
        Button(
            onClick = {
                callActivity()
            },
            enabled = true,
            modifier = Modifier
                .padding(16.dp)
                .size(width = 200.dp, height = 50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Text(text = text)
        }
    }
}