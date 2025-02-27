package com.example.toasty.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.toasty.interfaces.ProductDao
import com.example.toasty.interfaces.ProductListKeyDao
import com.example.toasty.models.Product
import com.example.toasty.models.ProductListKey

@Database(entities = [Product::class, ProductListKey::class], version = 1, exportSchema = false)
abstract class ProductListDatabase: RoomDatabase() {

    companion object{
        fun getInstance(context: Context): ProductListDatabase{
            return Room.databaseBuilder(context,ProductListDatabase::class.java, "product_db").build()
        }
    }

    abstract fun getApiProductListDao(): ProductDao

    abstract fun getProductListKeyDao(): ProductListKeyDao
}