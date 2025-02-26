package com.example.toasty.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TestItem(
    @PrimaryKey(autoGenerate = false)
    val id:Int=0,
    val title:String?=null,
    val body:String?=null
)
