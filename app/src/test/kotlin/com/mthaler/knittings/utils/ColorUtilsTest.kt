package com.mthaler.knittings.utils

import android.graphics.Color
import org.junit.Assert
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun testColorToHex() {
        Assert.assertEquals("#FFFF0000", Color.RED.colorToHex())
        Assert.assertEquals("#FF00FF00", Color.GREEN.colorToHex())
        Assert.assertEquals("#FF0000FF", Color.BLUE.colorToHex())
        Assert.assertEquals("#FFFFFFFF", Color.WHITE.colorToHex())
        Assert.assertEquals("#FF000000", Color.BLACK.colorToHex())
    }
}