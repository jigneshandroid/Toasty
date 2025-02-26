package com.example.toasty.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.toasty.interfaces.TestItemDao
import com.example.toasty.interfaces.TestItemKeyDao
import com.example.toasty.models.TestItem
import com.example.toasty.models.TestItemKey

@Database(entities = [TestItem::class, TestItemKey::class], version = 1, exportSchema = false)
abstract class TestItemDatabase: RoomDatabase() {

    companion object {
        fun getInstance(context: Context): TestItemDatabase {
            return Room.databaseBuilder(context, TestItemDatabase::class.java, "test_item_db")
                .build()
        }
    }
    abstract fun getTestItemDao(): TestItemDao

    abstract fun getTestItemKeyDao(): TestItemKeyDao
}