package com.mthaler.knittings.utils

import java.io.*
import java.text.SimpleDateFormat
import java.util.Date

fun File.copy(dst: File) {
    var inStream: InputStream? = null
    var outStream: OutputStream? = null
    try {
        inStream = FileInputStream(this)
        outStream = FileOutputStream(dst)
        val buffer = ByteArray(1024 * 4)
        var length: Int = inStream.read(buffer)
        while (length > 0) {
            outStream.write(buffer, 0, length)
            length = inStream.read(buffer)
        }
    } finally {
        inStream?.close()
        outStream?.close()
    }
}

fun Date.createDateTimeDirectoryName(): String {
    val format = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
    return format.format(this)
}

fun String.getExtension(): String {
    val index = this.lastIndexOf(".")
    return if (index >= 0) this.substring(index + 1) else ""
}

fun String.getFilenameWithoutExtension(): String {
    val index = this.lastIndexOf(".")
    return if (index >= 0) this.substring(0, index) else this
}