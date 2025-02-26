package com.example.toasty.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TestUser(
    @PrimaryKey(autoGenerate = false)
    val id:Int,
    val title:String?,
    val body:String?
)
