package com.mthaler.knittings.utils

import org.junit.Test
import org.junit.Assert.*
import com.mthaler.knittings.utils.StringUtils.formatBytes

class StringUtilsTest {

    @Test
    fun testFormatBytes() {
        assertEquals("1 B", formatBytes(1))
        assertEquals("10 B", formatBytes(10))
        assertEquals("100 B", formatBytes(100))
        assertEquals("1000 B", formatBytes(1000))
        assertEquals("1.0 KB", formatBytes(1024))
        assertEquals("2.0 KB", formatBytes(2048))
        assertEquals("10.0 KB", formatBytes(10240))
        assertEquals("100.0 KB", formatBytes(102400))
        assertEquals("1000.0 KB", formatBytes(1024000))
        assertEquals("1.0 MB", formatBytes(1024 * 1024))
        assertEquals("10.0 MB", formatBytes(1024 * 1024 * 10))
        assertEquals("100.0 MB", formatBytes(1024 * 1024 * 100))
        assertEquals("1000.0 MB", formatBytes(1024 * 1024 * 1000))
        assertEquals("1.0 GB", formatBytes(1024L * 1024 * 1024))
        assertEquals("10.0 GB", formatBytes(1024L * 1024 * 1024 * 10))
        assertEquals("100.0 GB", formatBytes(1024L * 1024 * 1024 * 100))
        assertEquals("1000.0 GB", formatBytes(1024L * 1024 * 1024 * 1000))
    }
}