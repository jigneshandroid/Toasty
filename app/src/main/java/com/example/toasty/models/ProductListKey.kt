package com.example.toasty.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProductListKey(
    @PrimaryKey(autoGenerate = false)
    val id:Long,
    val prevKey:Int?,
    val nextKey:Int?
)
