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

object FileUtils {
    fun createDateTimeDirectoryName(d: Date): String {
        val format = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        return format.format(d)
    }

    /**
     * Replaces all characters that are not in the range [a-zA-Z_0-9] with underscores
     *
     * @param filename filename
     * @return filename with characters that are not in [a-zA-Z_0-9] replaced by underscores
     */
    fun replaceIllegalCharacters(filename: String): String = filename.replace("""[^\w.-]""".toRegex(), "_")

    /**
     *
     */
    fun getExtension(filename: String): String {
        val index = filename.lastIndexOf(".")
        return if (index >= 0) filename.substring(index + 1) else ""
    }

    fun getFilenameWithoutExtension(filename: String): String {
        val index = filename.lastIndexOf(".")
        return if (index >= 0) filename.substring(0, index) else filename
    }
}