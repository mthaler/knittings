package com.mthaler.knittings.model

import android.graphics.Color
import com.mthaler.knittings.database.Database
import com.mthaler.knittings.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.*

class DatabaseTest {

    @Test
    fun testSerializeDeserialize() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val knittings = listOf(Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0))
        val photos = listOf(Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null))
        val categories = listOf(Category(42, "test", null), Category(43, "test2", Color.RED))
        val needles = listOf(Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET))
        val db0 = Database(knittings, photos, categories, needles, emptyList())
        val db1 = SerializeUtils.deserialize<Database>(SerializeUtils.serialize(db0))
        assertEquals(db0, db1)
    }
}