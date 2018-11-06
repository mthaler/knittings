package com.mthaler.knittings.utils

import android.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun testColorToHex() {
        assertEquals("#FFFF0000",ColorUtils.colorToHex(Color.RED))
        assertEquals("#FF00FF00",ColorUtils.colorToHex(Color.GREEN))
        assertEquals("#FF0000FF",ColorUtils.colorToHex(Color.BLUE))
        assertEquals("#FFFFFFFF",ColorUtils.colorToHex(Color.WHITE))
        assertEquals("#FF000000",ColorUtils.colorToHex(Color.BLACK))
    }
}