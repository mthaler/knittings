package com.mthaler.knittings.database.table

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import com.mthaler.knittings.model.Photo
import org.jetbrains.anko.db.*
import java.io.File

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

    object Cols {
        val ID = "_id"
        val FILENAME = "filename"
        val PREVIEW = "preview"
        val DESCRIPTION = "description"
        val KNITTING_ID = "knitting_id"
    }


    fun cursorToPhoto(cursor: Cursor): Photo {
        val idIndex = cursor.getColumnIndex(Cols.ID)
        val idPreview = cursor.getColumnIndex(Cols.PREVIEW)
        val idFilename = cursor.getColumnIndex(Cols.FILENAME)
        val idKnittingIndex = cursor.getColumnIndex(Cols.KNITTING_ID)
        val idDescription = cursor.getColumnIndex(Cols.DESCRIPTION)

        val id = cursor.getLong(idIndex)
        val filename = cursor.getString(idFilename)
        val previewBytes = if (cursor.isNull(idPreview)) null else cursor.getBlob(idPreview)
        val knittingID = cursor.getLong(idKnittingIndex)
        val description = cursor.getString(idDescription)

        val photo = Photo(id, File(filename), knittingID, description)
        if (previewBytes != null) {
            val options = BitmapFactory.Options()
            val preview = BitmapFactory.decodeByteArray(previewBytes, 0, previewBytes.size, options)
            return photo.copy(preview = preview)
        } else {
            return photo
        }
    }
}