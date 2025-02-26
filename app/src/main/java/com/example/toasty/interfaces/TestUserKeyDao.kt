package com.example.toasty.interfaces

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.toasty.models.TestUserKey

@Dao
interface TestUserKeyDao {

    @Insert(entity = TestUserKey::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(testUserKey: List<TestUserKey>)

    @Query("DELETE FROM TestUserKey")
    suspend fun deleteAll()

    @Query("SELECT * FROM TestUserKey where id=:id")
    fun getTestUserKey(id: Int): TestUserKey
}