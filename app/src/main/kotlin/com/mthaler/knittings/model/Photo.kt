package com.mthaler.knittings.model

import android.graphics.Bitmap
<<<<<<< HEAD
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mthaler.knittings.database.table.PhotoTable
=======
>>>>>>> master
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * The photo class represents a photo. It has an id (used in the database), a filename and an optional preview
 */
<<<<<<< HEAD
@Entity(tableName = PhotoTable.PHOTOS)
=======
>>>>>>> master
data class Photo(
    val id: Long,
    val filename: File,
    val ownerID: Long,
    val description: String = "",
    val preview: Bitmap? = null) : Serializable {

    companion object {

        fun getBytes(preview: Bitmap?): ByteArray? {
            if (preview != null) {
                val out = ByteArrayOutputStream()
                preview.compress(Bitmap.CompressFormat.JPEG, 90, out)
                return out.toByteArray()
            } else {
                return null
            }
        }

        val photoFilename: String
            get() {
                val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date())
                return "IMG_$timeStamp.jpg"
            }
    }
}
