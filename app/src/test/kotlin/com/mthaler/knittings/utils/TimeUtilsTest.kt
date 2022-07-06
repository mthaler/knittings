package com.mthaler.knittings.utils

import org.junit.Test
import org.junit.Assert.*
import com.mthaler.knittings.utils.TimeUtils.formatDuration

class TimeUtilsTest {

    @Test
    fun testFormatDuration() {
        assertEquals("00:00:00", formatDuration(0))
        assertEquals("00:00:01", formatDuration(1 * 1000))
        assertEquals("00:00:10", formatDuration(10 * 1000))
        assertEquals("00:01:00", formatDuration(1* 60 * 1000))
        assertEquals("00:10:00", formatDuration(10 * 60 * 1000))
        assertEquals("01:00:00", formatDuration(1 * 60 * 60 * 1000))
        assertEquals("10:00:00", formatDuration(10 * 60 * 60 * 1000))
    }
}