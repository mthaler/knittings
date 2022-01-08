package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class RowCounter(val id: Long = -1, val totalRows: Int = 0, val rowsPerRepeat: Int = 0, val knittingID: Long = -1) : Serializable, Parcelable {

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

        val EMPTY = RowCounter()

        @JvmField
        val CREATOR = object : Parcelable.Creator<RowCounter> {
            override fun createFromParcel(parcel: Parcel) = RowCounter(parcel)
            override fun newArray(size: Int) = arrayOfNulls<RowCounter>(size)
        }
    }
}