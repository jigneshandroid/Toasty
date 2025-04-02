package com.example.toasty.common

import android.app.Activity
import android.content.Intent
import com.example.toasty.activities.ActivityLearnKotlin
import com.example.toasty.activities.ActivityMap
import com.example.toasty.activities.ChatGpyActivity
import com.example.toasty.activities.ComposeTestActivity
import com.example.toasty.activities.RemoteMediatorActivity
import com.example.toasty.arcore.LoadArDatabase
import com.example.toasty.tensorflow.ActivityTensorFlowDetectObject


fun callActivityLearnKotlin(activity: Activity){
        val intent = Intent(activity, ActivityLearnKotlin::class.java)
        activity.startActivity(intent)
}

fun callActivityMapSensor(activity: Activity){
        val intent = Intent(activity, ActivityMap::class.java)
        activity.startActivity(intent)
}

fun callActivityChatSpy(activity: Activity){
        val intent = Intent(activity, ChatGpyActivity::class.java)
        activity.startActivity(intent)
}

fun callActivityComposeTest(activity: Activity){
        val intent = Intent(activity, ComposeTestActivity::class.java)
        activity.startActivity(intent)
}

fun callActivityRemoteMediator(activity: Activity){
        val intent = Intent(activity, RemoteMediatorActivity::class.java)
        activity.startActivity(intent)
}

fun callActivityArCore(activity: Activity){
        val intent = Intent(activity, LoadArDatabase::class.java)
        activity.startActivity(intent)
}

fun callActivityTensorFlowLite(activity: Activity){
        val intent = Intent(activity, ActivityTensorFlowDetectObject::class.java)
        activity.startActivity(intent)
}