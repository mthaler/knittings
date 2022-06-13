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

    fun create(db: SQLiteDatabase) {
        val CREATE_KNITTING_TABLE = "CREATE TABLE $IF_NOT_EXISTS $KNITTINGS ( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.TITLE} $TEXT $NOT_NULL, " +
                "${Cols.DESCRIPTION} $TEXT $NOT_NULL, " +
                "${Cols.STARTED} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.FINISHED} $INTEGER, " +
                "${Cols.NEEDLE_DIAMETER} $TEXT $NOT_NULL, " +
                "${Cols.SIZE} $TEXT $NOT_NULL, " +
                "${Cols.DEFAULT_PHOTO_ID} $INTEGER, " +
                "${Cols.RATING} $REAL $NOT_NULL ${DEFAULT("0.0")}, " +
                "${Cols.DURATION} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.CATEGORY_ID} $INTEGER, " +
                "${Cols.STATUS} $TEXT $NOT_NULL, " +
                "${FOREIGN_KEY(Cols.DEFAULT_PHOTO_ID, PhotoTable.PHOTOS, PhotoTable.Cols.ID)}, " +
                "${FOREIGN_KEY(Cols.CATEGORY_ID, CategoryTable.CATEGORY, CategoryTable.Cols.ID)} )"
        db.execSQL(CREATE_KNITTING_TABLE)
    }
}