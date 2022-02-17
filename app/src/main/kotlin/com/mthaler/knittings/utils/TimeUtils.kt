package com.mthaler.knittings.utils

object TimeUtils {

    fun formatDuration(millis: Long): String {
        val seconds = millis / 1000 % 60
        val minutes = millis / (1000 * 60) % 60
        val hours = millis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun parseDuration(s: String): Long {
        val tokens = s.split(':')
        val hours = tokens[0].toLong()
        val minutes = tokens[1].toLong()
        val seconds = tokens[2].toLong()
        return 1000L * (hours * 3600 + minutes * 60 + seconds)
    }
}