package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import com.mthaler.knittings.utils.readBoolean
import com.mthaler.knittings.utils.writeBoolean
import java.io.Serializable

data class Needle(
    val id: Long = -1,
    val name: String = "",
    val description: String = "",
    val size: String = "",
    val length: String = "",
    val material: NeedleMaterial = NeedleMaterial.OTHER,
    val inUse: Boolean = false,
    val type: NeedleType = NeedleType.OTHER
) : Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            name = parcel.readString(),
            description = parcel.readString(),
            size = parcel.readString(),
            length = parcel.readString(),
            material = NeedleMaterial.values()[parcel.readInt()],
            inUse = parcel.readBoolean(),
            type = NeedleType.values()[parcel.readInt()]
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(size)
        parcel.writeString(length)
        parcel.writeInt(material.ordinal)
        parcel.writeBoolean(inUse)
        parcel.writeInt(type.ordinal)
    }

    override fun describeContents(): Int = 0

    companion object {

        val EMPTY = Needle()

        @JvmField
        val CREATOR = object : Parcelable.Creator<Needle> {
            override fun createFromParcel(parcel: Parcel) = Needle(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Needle>(size)
        }
    }
}