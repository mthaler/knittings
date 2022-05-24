package com.mthaler.knittings.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mthaler.knittings.database.dao.*
import com.mthaler.knittings.database.table.CategoryTable
import com.mthaler.knittings.database.table.KnittingTable
import com.mthaler.knittings.database.table.NeedleTable
import com.mthaler.knittings.database.table.RowCounterTable
import com.mthaler.knittings.model.*

@Database(entities = [Category::class, Knitting::class, Needle::class, Photo::class, RowCounter::class], version = 6)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun knittingDao(): KnittingDao

    abstract fun needleDao(): NeedleDao

    abstract fun photoDao(): PhotoDao

    abstract fun rowCounterDao(): RowCounterDao

    companion object {
         private val TAG = "AppDatabase"


        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(KnittingTable.SQL_ADD_DURATION)
                Log.i(TAG, "Added duration colomn to knitting table")
                database.execSQL(KnittingTable.SQL_ADD_CATEGORY)
                Log.i(TAG, "Added category ID colomn to knitting table")

                try {
                    CategoryTable.create(database)
                    Log.d(TAG, "Category table created")
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not create category table", ex)
                }
            }
        }


        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL(KnittingTable.SQL_ADD_STATUS)
                    Log.i(TAG, "Added status column to knitting table")
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not add status column to knitting table", ex)
                }

                try {
                    NeedleTable.create(database)
                    Log.d(TAG, "Needle table created")
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not create needle table", ex)
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(NeedleTable.SQL_ADD_TYPE)
                Log.i(TAG, "Added type column to needle table")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    RowCounterTable.create(database)
                    Log.d(TAG, "RowCounter table created")
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not create row counter table", ex)
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Empty implementation, because the schema isn't changing.
            }
        }

         @Volatile
         private var INSTANCE: AppDatabase? = null

         fun getDatabase(context: Context): AppDatabase {
              // if the INSTANCE is not null, then return it,
              // if it is, then create the database
              if (INSTANCE == null) {
                  synchronized(this) {
                      // Pass the database to the INSTANCE
                      INSTANCE = buildDatabase(context)
                  }
              }
              // Return database.
              return INSTANCE!!
         }

         private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "notes_database"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
        }
    }
}