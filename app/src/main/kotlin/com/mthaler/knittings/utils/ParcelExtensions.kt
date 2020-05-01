package com.mthaler.knittings.utils

import android.os.Parcel

fun Parcel.writeOptionalInt(value: Int?) {
    if (value != null) {
        writeByte(1)
        writeInt(value)
    } else {
        writeByte(0)
    }
}

fun Parcel.readOptionalInt(): Int? {
    val flag = readByte()
    if (flag == 1.toByte()) {
        return readInt()
    } else {
        return null
    }
}