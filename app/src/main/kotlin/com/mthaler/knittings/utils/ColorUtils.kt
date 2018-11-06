package com.mthaler.knittings.utils

object ColorUtils {

    /**
     * Formats a color as hex string
     *
     * @param color
     * @return hex string
     */
    fun colorToHex(color: Int): String = String.format("#%08X", color)
}
