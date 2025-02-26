package com.example.toasty.interfaces

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.toasty.models.TestItem
import com.example.toasty.models.TestItemKey
import okhttp3.internal.Version

@Dao
interface TestItemKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(itemKeys: List<TestItemKey>)

    @Query("DELETE FROM TestItemKey")
    suspend fun deleteAll()

    @Query("SELECT * FROM TestItemKey where id = :id")
    fun getItemKey(id: Int): TestItemKey

}