package com.mthaler.knittings.utils

import android.graphics.Color
import org.junit.Assert
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun testColorToHex() {
        Assert.assertEquals("#FFFF0000", ColorUtils.colorToHex(Color.RED))
        Assert.assertEquals("#FF00FF00", ColorUtils.colorToHex(Color.GREEN))
        Assert.assertEquals("#FF0000FF", ColorUtils.colorToHex(Color.BLUE))
        Assert.assertEquals("#FFFFFFFF", ColorUtils.colorToHex(Color.WHITE))
        Assert.assertEquals("#FF000000", ColorUtils.colorToHex(Color.BLACK))
    }
}