package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.database.table.*
import com.mthaler.knittings.model.Knitting

/**
 * Class that defines the knittings database table schema
 */
object KnittingTable {
    const val KNITTINGS = "knittings"

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

    val SQL_ADD_DURATION = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.DURATION + " INTEGER NOT NULL DEFAULT 0"
    val SQL_ADD_CATEGORY = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.CATEGORY_ID + " INTEGER"
    val SQL_ADD_STATUS = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.STATUS + " TEXT"
}