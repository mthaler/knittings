package com.mthaler.knittings.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.Serializable

/**
 * The photo class represents a photo. It has an id (used in the database), a filename and an
 * optional preview that can be displayed in the knittings list
 */
data class Photo(val id: Long, val filename: File, val knittingID: Long, val description: String = "", val preview: Bitmap? = null) : Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            filename = File(parcel.readString()),
            knittingID = parcel.readLong(),
            description = parcel.readString(),
            preview = parcel.readParcelable(classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(filename.absolutePath)
        parcel.writeLong(knittingID)
        parcel.writeString(description)
        parcel.writeParcelable(preview, 0)

    }

    override fun describeContents(): Int = 0

    companion object {

        val classLoader: ClassLoader = javaClass.classLoader

        @JvmField
        val CREATOR = object : Parcelable.Creator<Photo> {
            override fun createFromParcel(parcel: Parcel) = Photo(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Photo>(size)
        }

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
