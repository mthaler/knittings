package com.mthaler.knittings.model

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.*

class JSONTest {

    @Test
    fun testKnittingToJSON() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, 3.0, 41.0, null, 5.0)
        val j0 = k0.toJSON()
        assertEquals(42, j0.getLong("id"))
        assertEquals("knitting", j0.getString("title"))
        assertEquals("my first knitting", j0.getString("description"))
        assertEquals("2018-01-10", j0.getString("started"))
        assertTrue(j0.isNull("finished"))
        assertEquals(3.0, j0.getDouble("needleDiameter"), 0.000001)
        assertEquals(41.0, j0.getDouble("size"), 0.000001)
        assertEquals(5.0, j0.getDouble("rating"), 0.000001)
    }

    @Test
    fun testPhotoToJSON() {
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val j0 = p0.toJSON()
        assertEquals(42, j0.getLong("id"))
        assertEquals("/tmp/photo1.jpg", j0.getString("filename"))
        assertEquals(43, j0.getLong("knittingID"))
        assertEquals("socks", j0.getString("description"))
    }
}