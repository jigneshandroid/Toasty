package com.example.toasty.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    @PrimaryKey(autoGenerate = false)
    val productId:Long,
    val categoryId:Long,
    val sponsorShopId:Long,
    val sponsorProductId:Long,
    val localPrice:Long,
    val name:String,
    val categoryName:String,
    val imgUrl:String
)
