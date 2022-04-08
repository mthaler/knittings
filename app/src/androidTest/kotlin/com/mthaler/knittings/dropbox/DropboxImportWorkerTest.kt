package com.mthaler.knittings.dropbox

import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.*

class DropboxImportWorkerTest {

    @Test
    fun testData() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        c.set(2018, 0, 11)
        val finished = c.time
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val k1 = Knitting(42, "knitting", "my first knitting", started, finished, "3.0", "41.0", p0, 5.0)
        val db = Database(listOf(k0, k1), listOf(p0), emptyList(), emptyList(), emptyList())
        val data = DropboxImportWorker.data("test", db)
        assertEquals(true, data.hasKeyWithValueOfType(DropboxImportWorker.Directory, String::class.java))
        assertEquals(true, data.hasKeyWithValueOfType(DropboxImportWorker.Database, String::class.java))
        assertEquals("test", data.getString(DropboxImportWorker.Directory))
    }
}