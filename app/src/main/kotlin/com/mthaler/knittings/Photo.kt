package com.mthaler.knittings

import android.graphics.Bitmap

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.Serializable

/**
 * The photo class represents a photo. It has an id (used in the database), a filename and an
 * optional preview that can be displayed in the knittings list
 */
class Photo(val id: Long, val filename: File, val knittingID: Long) : Serializable {
    var preview: Bitmap? = null
    var description: String? = null

    init {
        this.description = ""
    }

    override fun toString(): String {
        return "Photo{" +
                "id=" + id +
                ", filename=" + filename +
                ", knittingID=" + knittingID +
                ", description='" + description + '\'' +
                '}'
    }

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
