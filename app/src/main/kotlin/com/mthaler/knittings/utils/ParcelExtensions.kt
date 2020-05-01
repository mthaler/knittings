package com.mthaler.knittings.utils

import android.os.Parcel
import java.io.IOException

fun Parcel.writeBoolean(value: Boolean) {
    if (value) {
        writeByte(1)
    } else {
        writeByte(0)
    }
}

fun Parcel.readBoolean(): Boolean {
    val value = readByte()
    if (value == 1.toByte()) {
        return true
    } else if (value == 0.toByte()) {
        return false
    } else {
        throw IOException("Value $value not a valid boolean")
    }
}

fun Parcel.writeOptionalInt(value: Int?) {
    if (value != null) {
        writeBoolean(true)
        writeInt(value)
    } else {
        writeBoolean(false)
    }
}

fun Parcel.readOptionalInt(): Int? {
    if (readBoolean()) {
        return readInt()
    } else {
        return null
    }
}

fun Parcel.writeOptionalLong(value: Long?) {
    if (value != null) {
        writeBoolean(true)
        writeLong(value)
    } else {
        writeBoolean(false)
    }
}

fun Parcel.readOptionalLong(): Long? {
    if (readBoolean()) {
        return readLong()
    } else {
        return null
    }
}