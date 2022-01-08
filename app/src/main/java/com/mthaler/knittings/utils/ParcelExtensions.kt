package com.mthaler.knittings.utils

import android.os.Parcel
import java.io.IOException

fun Parcel.writeBooleanCompat(value: Boolean) {
    if (value) {
        writeByte(1)
    } else {
        writeByte(0)
    }
}

fun Parcel.readBooleanCompat(): Boolean {
    return when (val value = readByte()) {
        1.toByte() -> true
        0.toByte() -> false
        else -> throw IOException("Value $value not a valid boolean")
    }
}

fun Parcel.writeOptionalInt(value: Int?) {
    if (value != null) {
        writeBooleanCompat(true)
        writeInt(value)
    } else {
        writeBooleanCompat(false)
    }
}

fun Parcel.readOptionalInt(): Int? {
    if (readBooleanCompat()) {
        return readInt()
    } else {
        return null
    }
}

fun Parcel.writeOptionalLong(value: Long?) {
    if (value != null) {
        writeBooleanCompat(true)
        writeLong(value)
    } else {
        writeBooleanCompat(false)
    }
}

fun Parcel.readOptionalLong(): Long? {
    if (readBooleanCompat()) {
        return readLong()
    } else {
        return null
    }
}