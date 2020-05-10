package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Database(val knittings: List<Knitting>, val photos: List<Photo>, val categories: List<Category>, val needles: List<Needle>) : Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            knittings = parcel.readParcelableArray(classLoader).map { it as Knitting },
            photos = parcel.readParcelableArray(classLoader).map { it as Photo },
            categories = parcel.readParcelableArray(classLoader).map { it as Category },
            needles = parcel.readParcelableArray(classLoader).map { it as Needle }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableArray(knittings.toTypedArray(), 0)
        parcel.writeParcelableArray(photos.toTypedArray(), 0)
        parcel.writeParcelableArray(categories.toTypedArray(), 0)
        parcel.writeParcelableArray(needles.toTypedArray(), 0)
    }

    override fun describeContents(): Int = 0

    companion object {

        val classLoader = javaClass.classLoader

        @JvmField
        val CREATOR = object : Parcelable.Creator<Database> {
            override fun createFromParcel(parcel: Parcel) = Database(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Database>(size)
        }
    }
}