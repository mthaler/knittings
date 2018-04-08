package com.mthaler.knittings.utils

object StringUtils {

    /**
     * Format bytes as a human-readable string
     */
    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return bytes.toString() + " B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}