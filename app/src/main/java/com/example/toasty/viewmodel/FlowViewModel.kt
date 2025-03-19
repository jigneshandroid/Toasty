package com.example.toasty.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlowViewModel: ViewModel() {

    val stateFlow = MutableStateFlow(0)
    val sharedFlow = MutableSharedFlow<Int>(
        replay = 4,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ).stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0
    )

    val channel = Channel<Int>(
        capacity = 3,
        onBufferOverflow = BufferOverflow.SUSPEND,
        onUndeliveredElement = {
            Log.d("ChatSpyActivity", "onUndeliveredElement: $it")
        }
    )
    val channelFlow = channel.receiveAsFlow()
    init{
        //initChannelFlow()
        initStateFlow()
        //initSharedFlow()
    }

   private fun initStateFlow(){
        viewModelScope.launch {
            repeat(10){
                delay(1000)
                stateFlow.emit(it)
            }
        }
    }

    /*    private fun initSharedFlow(){
           viewModelScope.launch {
               repeat(10){
                   delay(1000)
                   sharedFlow.emit(it)
               }
           }
       }*/

    private fun initChannelFlow(){
        viewModelScope.launch {
            repeat(10){
                //delay(1000)
                channel.send(it)
            }
        }
    }

    fun firebaseAuth() = callbackFlow<String> {
        trySend("Hello world")
        trySend("Hello world")
        awaitClose {  }
    }
}