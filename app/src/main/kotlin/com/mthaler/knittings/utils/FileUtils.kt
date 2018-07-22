package com.mthaler.knittings.utils

import java.text.SimpleDateFormat
import java.util.*

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
        if (index >= 0) {
            return filename.substring(index + 1)
        } else {
            return "";
        }
    }
}