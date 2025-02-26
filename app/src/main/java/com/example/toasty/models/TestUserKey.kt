package com.example.toasty.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TestUserKey(
    @PrimaryKey(autoGenerate = false)
    val id:Int,
    val prevKey:Int?,
    val nextKey:Int?
)
