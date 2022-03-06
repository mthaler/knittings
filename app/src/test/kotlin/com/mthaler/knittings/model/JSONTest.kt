package com.mthaler.knittings.model

import org.junit.Test
import java.io.File

class JSONTest {

    @Test
    fun testPhotoToJSON() {
        val p = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val j = p.toJSON()
    }
}