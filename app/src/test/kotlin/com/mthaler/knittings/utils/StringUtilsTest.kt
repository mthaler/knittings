package com.mthaler.knittings.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun testRemoveLeadingChar() {
        assertEquals("test", StringUtils.removeLeadingChar("test", '/'))
    }
}