package com.mthaler.knittings.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 * Database helper class that defines our tables, columns and methods to create and drop tables
 */
class KnittingDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION), AnkoLogger {

    /**
     * Class that defines the knittings database table schema
     */
    object KnittingTable {
        val KNITTINGS = "knittings"

        val Columns = arrayOf(Cols.ID, Cols.TITLE, Cols.DESCRIPTION, Cols.STARTED, Cols.FINISHED, Cols.NEEDLE_DIAMETER, Cols.SIZE, Cols.DEFAULT_PHOTO_ID, Cols.RATING)

        val SQL_CREATE = "CREATE TABLE " + KNITTINGS +
                "(" + Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Cols.TITLE + " TEXT NOT NULL, " +
                Cols.DESCRIPTION + " TEXT NOT NULL, " +
                Cols.STARTED + " INTEGER NOT NULL DEFAULT 0, " +
                Cols.FINISHED + " INTEGER, " +
                Cols.NEEDLE_DIAMETER + " REAL NOT NULL DEFAULT 0.0, " +
                Cols.SIZE + " REAL NOT NULL DEFAULT 0.0, " +
                Cols.DEFAULT_PHOTO_ID + " INTEGER, " +
                Cols.RATING + " REAL NOT NULL DEFAULT 0.0, " +
                "FOREIGN KEY(" + Cols.DEFAULT_PHOTO_ID + ") REFERENCES " + PhotoTable.PHOTOS + "(" + PhotoTable.Cols.ID + "));"

        val SQL_DROP = "DROP TABLE IF EXISTS " + KNITTINGS

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
        }
    }

    object PhotoTable {
        val PHOTOS = "photos"

        val Columns = arrayOf(Cols.ID, Cols.FILENAME, Cols.PREVIEW, Cols.DESCRIPTION, Cols.KNITTING_ID)

        val SQL_CREATE = "CREATE TABLE " + PHOTOS +
                "(" + Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Cols.FILENAME + " TEXT NOT NULL, " +
                Cols.PREVIEW + " BLOB, " +
                Cols.DESCRIPTION + " TEXT NOT NULL, " +
                Cols.KNITTING_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + Cols.KNITTING_ID + ") REFERENCES " + KnittingTable.KNITTINGS + "(" + KnittingTable.Cols.ID + "));"

        val SQL_DROP = "DROP TABLE IF EXISTS " + PHOTOS

        object Cols {
            val ID = "_id"
            val FILENAME = "filename"
            val PREVIEW = "preview"
            val DESCRIPTION = "description"
            val KNITTING_ID = "knitting_id"
        }
    }

    init {
        debug("KnittingDatabaseHelper created database: " + databaseName)
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(KnittingTable.SQL_CREATE)
            debug("Knitting table created with: " + KnittingTable.SQL_CREATE)
        } catch (ex: Exception) {
            error("Could not create knitting table with: " + KnittingTable.SQL_CREATE, ex)
        }

        try {
            db.execSQL(PhotoTable.SQL_CREATE)
            debug("Photo table created with: " + PhotoTable.SQL_CREATE)
        } catch (ex: Exception) {
            error("Could not create photo table with: " + PhotoTable.SQL_CREATE, ex)
        }

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        debug("Knitting table with version number $oldVersion will be dropped.")
        db.execSQL(KnittingTable.SQL_DROP)
        debug("Photo table with version number $oldVersion will be dropped.")
        db.execSQL(PhotoTable.SQL_DROP)
        onCreate(db)
    }

    companion object {
        private val DB_NAME = "knittings.db"
        private val DB_VERSION = 1
    }
}
