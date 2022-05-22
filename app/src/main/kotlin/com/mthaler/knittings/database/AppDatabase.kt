package com.mthaler.knittings.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mthaler.knittings.database.dao.CategoryDao
import com.mthaler.knittings.model.*

@Database(entities = [Category::class, Knitting::class, Needle::class, Photo::class, RowCounter::class], version = 6)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
}