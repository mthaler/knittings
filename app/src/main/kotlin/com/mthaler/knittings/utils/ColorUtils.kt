package com.mthaler.knittings.utils

object ColorUtils {

    fun ColorToHex(color: Int): String {
        val alpha = android.graphics.Color.alpha(color)
        val blue = android.graphics.Color.blue(color)
        val green = android.graphics.Color.green(color)
        val red = android.graphics.Color.red(color)

        val alphaHex = To00Hex(alpha)
        val blueHex = To00Hex(blue)
        val greenHex = To00Hex(green)
        val redHex = To00Hex(red)

        // hexBinary value: aabbggrr
        val str = StringBuilder("#")
        str.append(alphaHex)
        str.append(blueHex)
        str.append(greenHex)
        str.append(redHex)

        return str.toString()
    }

    private fun To00Hex(value: Int): String {
        val hex = "00" + Integer.toHexString(value)
        return hex.substring(hex.length - 2, hex.length)
    }
}
