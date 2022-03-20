package com.mthaler.knittings.filter

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.*

class ContainsFilterTest {



    @Test
     fun filter() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        c.set(2018, 0, 11)
        val finished = c.time
        val k1 = Knitting(42, "test", "test", started, finished, "3.0", "41.0", p0, 5.0)
        val f = ContainsFilter("Knitting")
        val result = f.filter(listOf(k0, k1))
        assertEquals(result, listOf(k0))
     }
}