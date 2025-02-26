package com.example.toasty.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TestItemKey(
    @PrimaryKey(autoGenerate = false)
    val id:Int,
    val next:Int?,
    val prev:Int?
)
