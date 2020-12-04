package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.mthaler.dbapp.database.table.*
import com.mthaler.dbapp.model.Photo

object PhotoTable {

    val PHOTOS = "photos"

    object Cols {
        val ID = "_id"
        val FILENAME = "filename"
        val PREVIEW = "preview"
        val DESCRIPTION = "description"
        val KNITTING_ID = "knitting_id"
    }

    val Columns = arrayOf(Cols.ID, Cols.FILENAME, Cols.PREVIEW, Cols.DESCRIPTION, Cols.KNITTING_ID)

    fun create(db: SQLiteDatabase) {
        val CREATE_PHOTO_TABLE = "CREATE TABLE $IF_NOT_EXISTS $PHOTOS ( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.FILENAME} $TEXT $NOT_NULL, " +
                "${Cols.PREVIEW} $BLOB, " +
                "${Cols.DESCRIPTION} $TEXT $NOT_NULL, " +
                "${Cols.KNITTING_ID} $INTEGER $NOT_NULL, " +
                "${FOREIGN_KEY(Cols.KNITTING_ID, KnittingTable.KNITTINGS, KnittingTable.Cols.ID)} )"
        db.execSQL(CREATE_PHOTO_TABLE)
    }

    fun createContentValues(photo: Photo, manualID: Boolean = false): ContentValues {
        val values = ContentValues()
        if (manualID) {
            values.put(Cols.ID, photo.id)
        }
        values.put(Cols.FILENAME, photo.filename.absolutePath)
        values.put(Cols.KNITTING_ID, photo.ownerID)
        values.put(Cols.DESCRIPTION, photo.description)
        val previewBytes = Photo.getBytes(photo.preview)
        if (previewBytes != null) {
            values.put(Cols.PREVIEW, previewBytes)
        } else {
            values.putNull(Cols.PREVIEW)
        }
        return values
    }
}