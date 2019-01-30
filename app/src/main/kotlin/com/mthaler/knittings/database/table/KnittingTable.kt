package com.mthaler.knittings.database.table

import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

/**
 * Class that defines the knittings database table schema
 */
object KnittingTable {
    val KNITTINGS = "knittings"

    val Columns = arrayOf(Cols.ID, Cols.TITLE, Cols.DESCRIPTION, Cols.STARTED, Cols.FINISHED, Cols.NEEDLE_DIAMETER, Cols.SIZE, Cols.DEFAULT_PHOTO_ID, Cols.RATING, Cols.DURATION, Cols.CATEGORY_ID)

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
        val CATEGORY_ID = "category_ID"
        val STATUS = "status"
    }

    fun create(db: SQLiteDatabase) {
        db.createTable(KNITTINGS, true,
                Cols.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                Cols.TITLE to TEXT + NOT_NULL,
                Cols.DESCRIPTION to TEXT + NOT_NULL,
                Cols.STARTED to INTEGER + NOT_NULL + DEFAULT("0"),
                Cols.FINISHED to INTEGER,
                Cols.NEEDLE_DIAMETER to TEXT + NOT_NULL,
                Cols.SIZE to TEXT + NOT_NULL,
                Cols.DEFAULT_PHOTO_ID to INTEGER,
                Cols.RATING to REAL + NOT_NULL + DEFAULT("0.0"),
                Cols.DURATION to INTEGER + NOT_NULL + DEFAULT("0"),
                Cols.CATEGORY_ID to INTEGER,
                FOREIGN_KEY(Cols.DEFAULT_PHOTO_ID, PhotoTable.PHOTOS, PhotoTable.Cols.ID),
                FOREIGN_KEY(Cols.CATEGORY_ID, CategoryTable.CATEGORY, CategoryTable.Cols.ID))
    }

    val SQL_ADD_DURATION = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.DURATION + " INTEGER NOT NULL DEFAULT 0"
    val SQL_ADD_CATEGORY = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.CATEGORY_ID + " INTEGER"
    val SQL_ADD_STATUS = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.STATUS + " STRING"
}