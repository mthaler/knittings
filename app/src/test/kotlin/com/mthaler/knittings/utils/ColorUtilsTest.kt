package com.mthaler.knittings.utils

import android.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun testColorToHex() {
        val hex0 = ColorUtils.colorToHex(Color.RED)
        println(hex0)
    }
}