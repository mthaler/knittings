package com.mthaler.knittings.utils

object StringUtils {

    /**
     * Format bytes as a human-readable string
     */
    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    fun containsIgnoreCase(src: String, what: String): Boolean {
        val length = what.length
        if (length == 0)
            return true // Empty string is contained

        val firstLo = Character.toLowerCase(what[0])
        val firstUp = Character.toUpperCase(what[0])

        for (i in src.length - length downTo 0) {
            // Quick check before calling the more expensive regionMatches() method:
            val ch = src[i]
            if (ch != firstLo && ch != firstUp)
                continue

            if (src.regionMatches(i, what, 0, length, ignoreCase = true))
                return true
        }

        return false
    }
}