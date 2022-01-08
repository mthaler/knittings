package com.mthaler.knittings.database

import android.graphics.BitmapFactory
import com.mthaler.knittings.database.table.PhotoTable
import com.mthaler.knittings.model.Photo
import java.io.File

object PhotoConverter {

    fun convert(dbRow: Map<String, Any?>): Photo {
        val id = dbRow.getLong(PhotoTable.Cols.ID)
        val filename = dbRow.getString(PhotoTable.Cols.FILENAME)
        val previewBytes = if (dbRow.containsKey(PhotoTable.Cols.PREVIEW) && dbRow.get(PhotoTable.Cols.PREVIEW) != null) dbRow.getBlob(PhotoTable.Cols.PREVIEW)  else null
        val knittingID = dbRow.getLong(PhotoTable.Cols.KNITTING_ID)
        val description = dbRow.getString(PhotoTable.Cols.DESCRIPTION)

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