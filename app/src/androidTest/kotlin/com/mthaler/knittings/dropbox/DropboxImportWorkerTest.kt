package com.mthaler.knittings.dropbox

import android.app.Application
import androidx.test.core.app.launchActivity
import com.mthaler.knittings.MyApplication
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*

class DropboxImportWorkerTest {


    private lateinit var application: Application

    @Before
    fun setUp() {
        launchActivity<DropboxImportActivity>().use { scenario ->
            scenario.onActivity { activity ->
                application = activity.application
            }
        }
    }

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

    @Test
    fun readDatabaseTest() {
        val app = application as MyApplication
        val db =DropboxImportWorker.readDatabase(app, "test", "com.mthaler.knittings.dropbox.database -> {\"version\":5,\"knittings\":[{\"id\":42,\"title\":\"knitting\",\"description\":\"my first knitting\",\"started\":\"2018-01-10\",\"needleDiameter\":\"3.0\",\"size\":\"41.0\",\"rating\":5,\"duration\":0,\"status\":\"PLANNED\"},{\"id\":42,\"title\":\"knitting\",\"description\":\"my first knitting\",\"started\":\"2018-01-10\",\"finished\":\"2018-01-11\",\"needleDiameter\":\"3.0\",\"size\":\"41.0\",\"rating\":5,\"defaultPhoto\":42,\"duration\":0,\"status\":\"PLANNED\"}],\"photos\":[{\"id\":42,\"filename\":\"\\/tmp\\/photo1.jpg\",\"knittingID\":43,\"description\":\"socks\"}],\"categories\":[],\"needles\":[],\"rowCounters\":[]}")
        assertEquals(true, db)
        print(db)
    }
}