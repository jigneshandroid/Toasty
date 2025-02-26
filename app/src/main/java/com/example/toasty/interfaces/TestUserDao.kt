package com.example.toasty.interfaces

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.toasty.models.TestUser

@Dao
interface TestUserDao {
    @Insert(entity = TestUser::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(testUserList: List<TestUser>)

    @Query("DELETE FROM TestUser")
    suspend fun deleteAll()

    @Query("SELECT * FROM TestUser")
    fun getTestUser(): PagingSource<Int, TestUser>
}