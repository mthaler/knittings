package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import com.mthaler.knittings.utils.readOptionalInt
import com.mthaler.knittings.utils.writeOptionalInt

data class Category(val id: Long = -1, val name: String = "", val color: Int? = null) : Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            name = parcel.readString(),
            color = parcel.readOptionalInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeOptionalInt(color)
    }

    override fun describeContents(): Int = 0

    companion object {

        val EMPTY = Category()

        @JvmField
        val CREATOR = object : Parcelable.Creator<Category> {
            override fun createFromParcel(parcel: Parcel) = Category(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Category>(size)
        }
    }
}