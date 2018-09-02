package com.mthaler.knittings.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
        private val DB_VERSION = 2

        private var instance: KnittingDatabaseHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): KnittingDatabaseHelper {
            if (instance == null) {
                instance = KnittingDatabaseHelper(ctx.applicationContext)
            }
            return instance!!
        }

    }

    /**
     * Class that defines the knittings database table schema
     */
    object KnittingTable {
        val KNITTINGS = "knittings"

        val Columns = arrayOf(Cols.ID, Cols.TITLE, Cols.DESCRIPTION, Cols.STARTED, Cols.FINISHED, Cols.NEEDLE_DIAMETER, Cols.SIZE, Cols.DEFAULT_PHOTO_ID, Cols.RATING, Cols.DURATION)

        val SQL_DROP = "DROP TABLE IF EXISTS $KNITTINGS"

        object Cols {
            val ID = "_id"
            val TITLE = "title"
            val DESCRIPTION = "description"
            val STARTED = "started"
            val FINISHED = "finished"
            val NEEDLE_DIAMETER = "needle_diameter"
            val SIZE = "size"
            val DEFAULT_PHOTO_ID = "default_photo_id"
            val RATING = "rating"
            val DURATION = "duration"
        }

        fun create(db: SQLiteDatabase) {
            db.createTable(KNITTINGS, true,
                    Cols.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                    Cols.TITLE to TEXT + NOT_NULL,
                    Cols.DESCRIPTION to TEXT + NOT_NULL,
                    Cols.STARTED to INTEGER + NOT_NULL + DEFAULT("0"),
                    Cols.FINISHED to INTEGER,
                    Cols.NEEDLE_DIAMETER to REAL + NOT_NULL + DEFAULT("0.0"),
                    Cols.SIZE to REAL + NOT_NULL + DEFAULT("0.0"),
                    Cols.DEFAULT_PHOTO_ID to INTEGER,
                    Cols.RATING to REAL + NOT_NULL + DEFAULT("0.0"),
                    Cols.DURATION to INTEGER + NOT_NULL + DEFAULT("0"),
                    FOREIGN_KEY(Cols.DEFAULT_PHOTO_ID, PhotoTable.PHOTOS, PhotoTable.Cols.ID))
        }

        val SQL_ADD_DURATION = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.DURATION + " INTEGER NOT NULL DEFAULT 0"
    }

    object PhotoTable {
        val PHOTOS = "photos"

        val Columns = arrayOf(Cols.ID, Cols.FILENAME, Cols.PREVIEW, Cols.DESCRIPTION, Cols.KNITTING_ID)

        fun create(db: SQLiteDatabase) {
            db.createTable(PHOTOS, true,
                    Cols.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                    Cols.FILENAME to TEXT + NOT_NULL,
                    Cols.PREVIEW to BLOB,
                    Cols.DESCRIPTION to TEXT + NOT_NULL,
                    Cols.KNITTING_ID to INTEGER + NOT_NULL,
                    FOREIGN_KEY(Cols.KNITTING_ID, KnittingTable.KNITTINGS, KnittingTable.Cols.ID))
        }

        val SQL_DROP = "DROP TABLE IF EXISTS $PHOTOS"

        object Cols {
            val ID = "_id"
            val FILENAME = "filename"
            val PREVIEW = "preview"
            val DESCRIPTION = "description"
            val KNITTING_ID = "knitting_id"
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

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            info("Updating knitting table from $oldVersion to $newVersion")
            db.execSQL(KnittingTable.SQL_ADD_DURATION)
            info("Added duration colomn to knitting table")
        }
    }
}

// Access property for Context
val Context.database: KnittingDatabaseHelper
    get() = KnittingDatabaseHelper.getInstance(applicationContext)
