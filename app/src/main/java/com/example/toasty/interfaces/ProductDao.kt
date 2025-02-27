package com.example.toasty.interfaces

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.toasty.models.Product

@Dao
interface ProductDao {
    @Insert(entity = Product::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductList(productList: List<Product>)

    @Query("DELETE FROM Product")
    suspend fun deleteAllProduct()

    @Query("SELECT * FROM Product")
    fun getProductList(): PagingSource<Int, Product>
}