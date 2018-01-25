package com.mthaler.knittings.utils

import org.junit.Test
import java.util.*
import org.junit.Assert.*
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName

class FileUtilsTest {

    @Test
    fun testCreateDateTimeDirectoryName() {
        val d = Date(1516860201129L)
        assertEquals("2018_01_25_07_03_21", createDateTimeDirectoryName(d))
    }
}