package com.example.toasty.interfaces

import androidx.paging.PagingSource
import androidx.room.*
import com.example.toasty.models.TestItem

@Dao
interface TestItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listItems: List<TestItem>)

    @Query("DELETE FROM TestItem")
    suspend fun delete()

    @Query("SELECT * FROM TestItem")
    fun getTestItems(): PagingSource<Int, TestItem>
}