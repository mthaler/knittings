package com.mthaler.knittings.utils

import android.graphics.Color
import org.junit.Assert
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun testColorToHex() {
        Assert.assertEquals("#FFFF0000", com.mthaler.knittings.utils.ColorUtils.colorToHex(Color.RED))
        Assert.assertEquals("#FF00FF00", com.mthaler.knittings.utils.ColorUtils.colorToHex(Color.GREEN))
        Assert.assertEquals("#FF0000FF", com.mthaler.knittings.utils.ColorUtils.colorToHex(Color.BLUE))
        Assert.assertEquals("#FFFFFFFF", com.mthaler.knittings.utils.ColorUtils.colorToHex(Color.WHITE))
        Assert.assertEquals("#FF000000", com.mthaler.knittings.utils.ColorUtils.colorToHex(Color.BLACK))
    }
}