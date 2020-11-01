package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Rows(val id: Long, val totalRows: Int, val rowsPerRepeat: Int, val knittingID: Long) : Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            totalRows = parcel.readInt(),
            rowsPerRepeat = parcel.readInt(),
            knittingID = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeInt(totalRows)
        parcel.writeInt(rowsPerRepeat)
        parcel.writeLong(knittingID)
    }

    override fun describeContents(): Int = 0

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<Rows> {
            override fun createFromParcel(parcel: Parcel) = Rows(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Rows>(size)
        }
    }
}