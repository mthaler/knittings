package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.mthaler.dbapp.database.table.*
import com.mthaler.knittings.model.Knitting

/**
 * Class that defines the knittings database table schema
 */
object KnittingTable {
    val KNITTINGS = "knittings"

    val Columns = arrayOf(Cols.ID, Cols.TITLE, Cols.DESCRIPTION, Cols.STARTED, Cols.FINISHED,
            Cols.NEEDLE_DIAMETER, Cols.SIZE, Cols.DEFAULT_PHOTO_ID, Cols.RATING, Cols.DURATION,
            Cols.CATEGORY_ID, Cols.STATUS)

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

    val SQL_ADD_DURATION = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.DURATION + " INTEGER NOT NULL DEFAULT 0"
    val SQL_ADD_CATEGORY = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.CATEGORY_ID + " INTEGER"
    val SQL_ADD_STATUS = "ALTER TABLE " + KNITTINGS + " ADD COLUMN " + Cols.STATUS + " TEXT"

    fun createContentValues(knitting: Knitting, manualID: Boolean = false): ContentValues {
        val values = ContentValues()
        if (manualID) {
            values.put(Cols.ID, knitting.id)
        }
        values.put(Cols.TITLE, knitting.title)
        values.put(Cols.DESCRIPTION, knitting.description)
        values.put(Cols.STARTED, knitting.started.time)
        if (knitting.finished != null) {
            values.put(Cols.FINISHED, knitting.finished.time)
        }
        values.put(Cols.NEEDLE_DIAMETER, knitting.needleDiameter)
        values.put(Cols.SIZE, knitting.size)
        if (knitting.defaultPhoto != null) {
            values.put(Cols.DEFAULT_PHOTO_ID, knitting.defaultPhoto.id)
        } else {
            values.putNull(Cols.DEFAULT_PHOTO_ID)
        }
        values.put(Cols.RATING, knitting.rating)
        values.put(Cols.DURATION, knitting.duration)
        if (knitting.category != null) {
            values.put(Cols.CATEGORY_ID, knitting.category.id)
        } else {
            values.putNull(Cols.CATEGORY_ID)
        }
        values.put(Cols.STATUS, knitting.status.name)
        return values
    }
}