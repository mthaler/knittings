package com.mthaler.knittings.utils

import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun createDateTimeDirectoryName(d: Date): String {
        val format = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        return format.format(d)
    }
}