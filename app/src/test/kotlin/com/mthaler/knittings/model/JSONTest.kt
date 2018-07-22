package com.mthaler.knittings.model

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class JSONTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

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
        assertFalse(j0.has("finished"))
        assertEquals(3.0, j0.getDouble("needleDiameter"), 0.000001)
        assertEquals(41.0, j0.getDouble("size"), 0.000001)
        assertEquals(5.0, j0.getDouble("rating"), 0.000001)
        assertTrue(j0.isNull("defaultPhoto"))
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        c.set(2018, 0, 11)
        val finished = c.time
        val k1 = Knitting(42, "knitting", "my first knitting", started, finished, 3.0, 41.0, p0, 5.0)
        val j1 = k1.toJSON()
        assertEquals(42, j1.getLong("id"))
        assertEquals("knitting", j1.getString("title"))
        assertEquals("my first knitting", j1.getString("description"))
        assertEquals("2018-01-10", j1.getString("started"))
        assertEquals("2018-01-11", j1.getString("finished"))
        assertEquals(3.0, j1.getDouble("needleDiameter"), 0.000001)
        assertEquals(41.0, j1.getDouble("size"), 0.000001)
        assertEquals(5.0, j1.getDouble("rating"), 0.000001)
        assertEquals(42, j1.getLong("defaultPhoto"))
    }

    @Test
    fun testPhotoToJSON() {
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val j0 = p0.toJSON()
        println(j0)
        assertEquals(42, j0.getLong("id"))
        assertEquals("/tmp/photo1.jpg", j0.getString("filename"))
        assertEquals(43, j0.getLong("knittingID"))
        assertEquals("socks", j0.getString("description"))
    }

    @Test
    fun testJSONToKnitting() {
        val s = """{"size":41,"needleDiameter":3,"rating":5,"description":"my first knitting","started":"2018-01-10","id":42,"title":"knitting"}"""
        val json = JSONObject(s)
        val k = json.toKnitting()
        assertEquals(42, k.id)
        assertEquals("knitting", k.title)
        assertEquals("my first knitting", k.description)
        assertEquals("2018-01-10", dateFormat.format(k.started))
        assertNull(k.finished)
        assertEquals(3.0, k.needleDiameter, 0.000001)
        assertEquals(41.0, k.size, 0.000001)
        assertEquals(5.0, k.rating, 0.000001)
    }

    @Test
    fun testJSONArrayToKnittings() {
        val s = """[{"size":41,"needleDiameter":3,"rating":5,"description":"my first knitting","started":"2018-01-10","id":42,"title":"knitting"},
            |{"size":42,"needleDiameter":3.5,"rating":4.5,"description":"my second knitting","started":"2018-01-11", "finished":"2018-01-12","id":43,"title":"knitting 2"}]
        """.trimMargin()
        val json = JSONArray(s)
        val knittings = json.toKnittings()
        assertEquals(2, knittings.size)
        val k = knittings[0]
        assertEquals(42, k.id)
        assertEquals("knitting", k.title)
        assertEquals("my first knitting", k.description)
        assertEquals("2018-01-10", dateFormat.format(k.started))
        assertNull(k.finished)
        assertEquals(3.0, k.needleDiameter, 0.000001)
        assertEquals(41.0, k.size, 0.000001)
        assertEquals(5.0, k.rating, 0.000001)
        val k2 = knittings[1]
        assertEquals(43, k2.id)
        assertEquals("knitting 2", k2.title)
        assertEquals("my second knitting", k2.description)
        assertEquals("2018-01-11", dateFormat.format(k2.started))
        assertEquals("2018-01-12", dateFormat.format(k2.finished))
        assertEquals(3.5, k2.needleDiameter, 0.000001)
        assertEquals(42.0, k2.size, 0.000001)
        assertEquals(4.5, k2.rating, 0.000001)
    }

    @Test
    fun testJSONToPhoto() {
        val s = """{"knittingID":43,"filename":"/tmp/photo1.jpg","description":"socks","id":42}"""
        val json = JSONObject(s)
        val p = json.toPhoto()
        assertEquals(42, p.id)
        assertEquals(File("/tmp/photo1.jpg"), p.filename)
        assertEquals(43, p.knittingID)
        assertEquals("socks", p.description)
    }

    @Test
    fun testJSONArrayToPhotos() {
        val s = """[{"knittingID":43,"filename":"/tmp/photo1.jpg","description":"socks","id":42},
            |{"knittingID":44,"filename":"/tmp/photo2.jpg","description":"shirt","id":43}]
        """.trimMargin()
        val json = JSONArray(s)
        val photos = json.toPhotos()
        assertEquals(2, photos.size)
        val p = photos[0]
        assertEquals(42, p.id)
        assertEquals(File("/tmp/photo1.jpg"), p.filename)
        assertEquals(43, p.knittingID)
        assertEquals("socks", p.description)
        val p2 = photos[1]
        assertEquals(43, p2.id)
        assertEquals(File("/tmp/photo2.jpg"), p2.filename)
        assertEquals(44, p2.knittingID)
        assertEquals("shirt", p2.description)
    }

    @Test
    fun testDatabaseToJSON() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, 3.0, 41.0, null, 5.0)
        c.set(2018, 0, 11)
        val started2 = c.time
        c.set(2018, 0, 12)
        val finished = c.time
        val k1 = Knitting(44, "knitting 2", "my second knitting", started2, finished, 3.5, 41.5, null, 4.5)
        val knittings = listOf(k0, k1)
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val p1 = Photo(43, File("/tmp/photo2.jpg"), 44, "shirt", null)
        val photos = listOf(p0, p1)
        val db = Database(knittings, photos)
        val json = db.toJSON()
    }
}