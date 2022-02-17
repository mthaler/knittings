package com.mthaler.knittings.model

import com.mthaler.knittings.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.*

class KnittingTest {

    @Test
    fun testSerializeDeserialize() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        val k1 = SerializeUtils.deserialize<Knitting>(SerializeUtils.serialize(k0))
        assertEquals(k0, k1)
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        c.set(2018, 0, 11)
        val finished = c.time
        val k2 = Knitting(42, "knitting", "my first knitting", started, finished, "3.0", "41.0", p0, 5.0)
        val k3 = SerializeUtils.deserialize<Knitting>(SerializeUtils.serialize(k2))
        assertEquals(k2, k3)
    }
}