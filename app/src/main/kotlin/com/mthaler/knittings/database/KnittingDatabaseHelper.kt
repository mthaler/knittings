package com.mthaler.knittings.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.database.table.CategoryTable
import com.mthaler.knittings.database.table.KnittingTable
import com.mthaler.knittings.database.table.NeedleTable
import com.mthaler.knittings.database.table.PhotoTable
import org.jetbrains.anko.*
import org.jetbrains.anko.db.*

/**
 * Database helper class that defines our tables, columns and methods to create and drop tables
 */
class KnittingDatabaseHelper(context: Context) : ManagedSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION), AnkoLogger {

    companion object {
        // Use in-memory database for testing. This is an ugly hack, but I didn't find another way to do this
        // if the database name is null, Android uses an in-memory database
        private val DB_NAME = try {
            Class.forName("android.support.test.espresso.Espresso")
            null
        } catch (_: ClassNotFoundException) {
            "knittings.db"
        }
        val DB_VERSION = 3

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
        debug("KnittingDatabaseHelper created database: $databaseName")
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            KnittingTable.create(db)
            debug("Knitting table created")
        } catch (ex: Exception) {
            error("Could not create knitting table", ex)
        }

        try {
            PhotoTable.create(db)
            debug("Photo table created")
        } catch (ex: Exception) {
            error("Could not create photo table", ex)
        }

        try {
            CategoryTable.create(db)
            debug("Category table created")
        } catch (ex: Exception) {
            error("Could not create category table", ex)
        }

        try {
            NeedleTable.create(db)
            debug("Needle table created")
        } catch (ex: Exception) {
            error("Could not create category needle", ex)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        info("Updating knitting table from $oldVersion to $newVersion")
        if (oldVersion == 1) {
            upgrade12(db)
            upgrade23(db)
        } else if (oldVersion == 2) {
            upgrade23(db)
        }
    }

    /**
     * Upgrades the database from version 1 to 2
     *
     * @param db database
     */
    private fun upgrade12(db: SQLiteDatabase) {
        db.execSQL(KnittingTable.SQL_ADD_DURATION)
        info("Added duration colomn to knitting table")
        db.execSQL(KnittingTable.SQL_ADD_CATEGORY)
        info("Added category ID colomn to knitting table")

        try {
            CategoryTable.create(db)
            debug("Category table created")
        } catch (ex: Exception) {
            error("Could not create category table", ex)
        }
    }

    /**
     * Upgrades the database from version 2 to 3
     *
     * @param db database
     */
    private fun upgrade23(db: SQLiteDatabase) {
        db.execSQL(KnittingTable.SQL_ADD_STATUS)
        info("Added status colomn to knitting table")

        try {
            NeedleTable.create(db)
            debug("Needle table created")
        } catch (ex: Exception) {
            error("Could not create needle table", ex)
        }
    }
}

// Access property for Context
val Context.database: KnittingDatabaseHelper
    get() = KnittingDatabaseHelper.getInstance(applicationContext)
