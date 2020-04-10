package com.mthaler.knittings.database.table

import android.content.ContentValues
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

    /**
     * Creates the content values map used to insert a photo into the database or update an existing photo
     *
     * @param photo photo
     * @param manualID should we manually insert id?
     * @return content values for inserting or updating needle
     */
    fun createContentValues(photo: Photo, manualID: Boolean = false): ContentValues {
        val values = ContentValues()
        if (manualID) {
            values.put(Cols.ID, photo.id)
        }
        values.put(Cols.FILENAME, photo.filename.absolutePath)
        values.put(Cols.KNITTING_ID, photo.knittingID)
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