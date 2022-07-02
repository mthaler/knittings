package com.mthaler.knittings.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

/**
 * The photo class represents a photo. It has an id (used in the database), a filename and an optional preview
 */
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

        fun getPhotoFilename(context: Context): String {
             val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date())
             val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
             if (storageDir != null) {
                 return "IMG_$timeStamp.jpg"
             } else {
                 throw IllegalArgumentException("Storage dir null")
             }
        }
    }
}
