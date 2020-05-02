package com.mthaler.knittings.model

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.Serializable

/**
 * The photo class represents a photo. It has an id (used in the database), a filename and an
 * optional preview that can be displayed in the knittings list
 */
data class Photo(val id: Long, val filename: File, val knittingID: Long, val description: String = "", val preview: Bitmap? = null) : Serializable {

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
    }
}
