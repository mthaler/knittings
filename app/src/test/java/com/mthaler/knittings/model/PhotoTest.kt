package com.mthaler.knittings.model

import com.mthaler.knittings.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PhotoTest {

    @Test
    fun testSerializeDeserialize() {
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val p1 = SerializeUtils.deserialize<Photo>(SerializeUtils.serialize(p0))
        assertEquals(p0, p1)
    }
}