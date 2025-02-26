package com.example.toasty.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.toasty.interfaces.TestUserDao
import com.example.toasty.interfaces.TestUserKeyDao
import com.example.toasty.models.TestUser
import com.example.toasty.models.TestUserKey

@Database(entities = [TestUser::class, TestUserKey::class], version = 2, exportSchema = false)
abstract class TestUserDatabase: RoomDatabase() {

    companion object{
        fun getInstance(context: Context): TestUserDatabase {
            return Room.databaseBuilder(context, TestUserDatabase::class.java, "test_user_db2").build()
        }
    }

    abstract fun getTestUserDao() : TestUserDao

    abstract fun getTestUserKeyDao() : TestUserKeyDao
}