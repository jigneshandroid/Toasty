package com.example.toasty.modeltypeconvertors

import androidx.room.TypeConverter
import com.example.toasty.models.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProductConvertor {
    @TypeConverter
    fun fromProductList(products: List<Product>): String {
        return Gson().toJson(products)
    }

    @TypeConverter
    fun toProductList(data: String): List<Product> {
        val listType = object : TypeToken<List<Product>>() {}.type
        return Gson().fromJson(data, listType)
    }
}