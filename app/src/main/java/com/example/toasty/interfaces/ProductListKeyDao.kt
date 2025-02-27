package com.example.toasty.interfaces

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.toasty.models.Product
import com.example.toasty.models.ProductListKey

@Dao
interface ProductListKeyDao {

    @Insert(entity = ProductListKey::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(listKey: List<ProductListKey>)

    @Query("DELETE FROM ProductListKey")
    suspend fun deleteAll()

    @Query("SELECT * FROM ProductListKey where id=:id")
    fun getProductList(id:Long): ProductListKey
}