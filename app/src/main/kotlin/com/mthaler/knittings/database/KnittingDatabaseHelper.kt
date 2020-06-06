package com.mthaler.knittings.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.mthaler.knittings.database.table.*

/**
 * Database helper class that defines our tables, columns and methods to create and drop tables
 */
class KnittingDatabaseHelper(context: Context) : ManagedSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {

        private const val TAG = "KnittingDatabaseHelper"

        // Use in-memory database for testing. This is an ugly hack, but I didn't find another way to do this
        // if the database name is null, Android uses an in-memory database
        private val DB_NAME = try {
            Class.forName("android.support.test.espresso.Espresso")
            null
        } catch (_: ClassNotFoundException) {
            "knittings.db"
        }
        val DB_VERSION = 5

        private var instance: KnittingDatabaseHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): KnittingDatabaseHelper {
            if (instance == null) {
                instance = KnittingDatabaseHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    init {
        Log.d(TAG, "KnittingDatabaseHelper created database: $databaseName")
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            KnittingTable.create(db)
            Log.d(TAG, "Knitting table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create knitting table", ex)
        }

        try {
            PhotoTable.create(db)
            Log.d(TAG, "Photo table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create photo table", ex)
        }

        try {
            CategoryTable.create(db)
            Log.d(TAG, "Category table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create category table", ex)
        }

        try {
            NeedleTable.create(db)
            Log.d(TAG, "Needle table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create needle table", ex)
        }
        try {
            RowsTable.create(db)
            Log.d(TAG, "Rows table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create rows table", ex)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "Updating knitting table from $oldVersion to $newVersion")
        when (oldVersion) {
            1 -> {
                upgrade12(db)
                upgrade23(db)
            }
            2 -> {
                upgrade23(db)
            }
            3 -> upgrade34(db)
            4 -> upgrade45(db)
        }
    }

    private fun upgrade12(db: SQLiteDatabase) {
        db.execSQL(KnittingTable.SQL_ADD_DURATION)
        Log.i(TAG, "Added duration colomn to knitting table")
        db.execSQL(KnittingTable.SQL_ADD_CATEGORY)
        Log.i(TAG, "Added category ID colomn to knitting table")

        try {
            CategoryTable.create(db)
            Log.d(TAG, "Category table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create category table", ex)
        }
    }

    private fun upgrade23(db: SQLiteDatabase) {
        try {
            db.execSQL(KnittingTable.SQL_ADD_STATUS)
            Log.i(TAG, "Added status column to knitting table")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not add status column to knitting table", ex)
        }

        try {
            NeedleTable.create(db)
            Log.d(TAG, "Needle table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create needle table", ex)
        }
    }

    private fun upgrade34(db: SQLiteDatabase) {
        db.execSQL(NeedleTable.SQL_ADD_TYPE)
        Log.i(TAG, "Added type column to needle table")
    }

    private fun upgrade45(db: SQLiteDatabase) {
        try {
            RowsTable.create(db)
            Log.d(TAG, "Rows table created")
        } catch (ex: Exception) {
            Log.e(TAG, "Could not create rows table", ex)
        }
    }
}

val Context.database: KnittingDatabaseHelper
    get() = KnittingDatabaseHelper.getInstance(applicationContext)
