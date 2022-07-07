package com.mthaler.knittings.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun testRemoveLeadingChars() {
        assertEquals("test", "test".removeLeadingChars('/'))
        assertEquals("test", "/test".removeLeadingChars('/'))
        assertEquals("test", "//test".removeLeadingChars('/'))
    }
}