package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import com.mthaler.dbapp.model.Category
import com.mthaler.dbapp.model.Photo
import com.mthaler.dbapp.model.Project
import com.mthaler.dbapp.utils.readOptionalLong
import com.mthaler.dbapp.utils.writeOptionalLong
import java.io.Serializable
import java.util.Date

/**
 * The Knitting class stores data for a single knitting
 */
data class Knitting(
        override val id: Long = -1,
        override val title: String = "",
        override val description: String = "",
        override val started: Date = Date(),
        val finished: Date? = null,
        val needleDiameter: String = "",
        val size: String = "",
        override val defaultPhoto: Photo? = null,
        val rating: Double = 0.0,
        val duration: Long = 0L,
        override val category: Category? = null,
        val status: Status = Status.PLANNED
) : Serializable, Parcelable, Project {

    private constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            title = parcel.readString()!!,
            description = parcel.readString()!!,
            started = Date(parcel.readLong()),
            finished = makeDate(parcel.readOptionalLong()),
            needleDiameter = parcel.readString()!!,
            size = parcel.readString()!!,
            defaultPhoto = parcel.readParcelable(classLoader),
            rating = parcel.readDouble(),
            duration = parcel.readLong(),
            category = parcel.readParcelable(classLoader),
            status = Status.values()[parcel.readInt()]
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeLong(started.time)
        parcel.writeOptionalLong(finished?.time)
        parcel.writeString(needleDiameter)
        parcel.writeString(size)
        parcel.writeParcelable(defaultPhoto, 0)
        parcel.writeDouble(rating)
        parcel.writeLong(duration)
        parcel.writeParcelable(category, 0)
        parcel.writeInt(status.ordinal)
    }

    override fun describeContents(): Int = 0

    companion object {

        val classLoader: ClassLoader = javaClass.classLoader!!

        val EMPTY = Knitting()

        @JvmField
        val CREATOR = object : Parcelable.Creator<Knitting> {
            override fun createFromParcel(parcel: Parcel) = Knitting(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Knitting>(size)
        }

        fun makeDate(time: Long?): Date? = if(time != null) Date(time) else null
    }
}
