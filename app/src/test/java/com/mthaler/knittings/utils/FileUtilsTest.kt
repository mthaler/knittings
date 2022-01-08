package com.mthaler.knittings.utils

import org.junit.Test
import java.util.*
import org.junit.Assert.*
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import com.mthaler.knittings.utils.FileUtils.getExtension
import com.mthaler.knittings.utils.FileUtils.getFilenameWithoutExtension
import com.mthaler.knittings.utils.FileUtils.replaceIllegalCharacters

class FileUtilsTest {

    @Test
    fun testCreateDateTimeDirectoryName() {
        val d = Date(1516860201129L)
        assertEquals("2018_01_25_07_03_21", createDateTimeDirectoryName(d))
    }

    @Test
    fun testReplaceIllegalCharacters() {
        assertEquals("abcde", replaceIllegalCharacters("abcde"))
        assertEquals("ABCDE", replaceIllegalCharacters("ABCDE"))
        assertEquals("abcDEF", replaceIllegalCharacters("abcDEF"))
        assertEquals("abc123", replaceIllegalCharacters("abc123"))
        assertEquals("abc-123", replaceIllegalCharacters("abc-123"))
        assertEquals("abc_def", replaceIllegalCharacters("abc:def"))
        assertEquals("abc_def", replaceIllegalCharacters("abc/def"))
        assertEquals("abc_def", replaceIllegalCharacters("abc_def"))
        assertEquals("abc_def", replaceIllegalCharacters("abc?def"))
        assertEquals("a_b_c_d_e", replaceIllegalCharacters("a:b:c:d:e"))
    }

    @Test
    fun testGetExtension() {
        assertEquals("", getExtension("test"))
        assertEquals("txt", getExtension("test.txt"))
        assertEquals("txt", getExtension(".txt"))
        assertEquals("txt", getExtension("test.orig.txt"))
    }

    @Test
    fun testGetFilenameWithoutExtension() {
        assertEquals("test", getFilenameWithoutExtension("test"))
        assertEquals("test", getFilenameWithoutExtension("test.txt"))
        assertEquals("", getFilenameWithoutExtension(".txt"))
        assertEquals("test.orig", getFilenameWithoutExtension("test.orig.txt"))
    }
}