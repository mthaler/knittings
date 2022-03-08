package com.mthaler.knittings.model

import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class JSONTest {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    @Test
    fun testKnittingToJSON() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
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
        val k1 = Knitting(42, "knitting", "my first knitting", started, finished, "3.0", "41.0", p0, 5.0)
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
    fun testJSONToKnitting() {
        val s = """{"size":41,"needleDiameter":3,"rating":5,"description":"my first knitting","started":"2018-01-10","id":42,"title":"knitting"}"""
        val json = JSONObject(s)
        val k = json.toKnitting(ApplicationProvider.getApplicationContext()).first
        assertEquals(42, k.id)
        assertEquals("knitting", k.title)
        assertEquals("my first knitting", k.description)
        assertEquals("2018-01-10", dateFormat.format(k.started))
        assertNull(k.finished)
        assertEquals("3", k.needleDiameter)
        assertEquals("41", k.size)
        assertEquals(5.0, k.rating, 0.000001)
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

    @Test
    fun testJSONToPhoto() {
        val s = """{"knittingID":43,"filename":"/tmp/photo1.jpg","description":"socks","id":42}"""
        val json = JSONObject(s)
        val p = json.toPhoto()
        assertEquals(42, p.id)
        assertEquals(File("/tmp/photo1.jpg"), p.filename)
        assertEquals(43, p.ownerID)
        assertEquals("socks", p.description)
    }

    @Test
    fun testNeedleToJSON() {
        val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET)
        val j0 = n0.toJSON()
        assertEquals(42, j0.getLong("id"))
        assertEquals("needle", j0.getString("name"))
        assertEquals("my first needle", j0.getString("description"))
        assertEquals("5 mm", j0.getString("size"))
        assertEquals("20 cm", j0.getString("length"))
        assertEquals("METAL", j0.getString("material"))
        assertTrue(j0.getBoolean("inUse"))
        assertEquals("SET", j0.getString("type"))
    }

    @Test
    fun testJSONToNeedle() {
        val s = """{"size":"5 mm","material":"METAL","name":"needle","length":"20 cm","inUse":true,"description":"my first needle","id":42,"type":"SET"}"""
        assertEquals(Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET), JSONObject(s).toNeedle(ApplicationProvider.getApplicationContext()))
    }

    @Test
    fun testJSONArrayToKnittings() {
        val s = """[{"size":41,"needleDiameter":3,"rating":5,"description":"my first knitting","started":"2018-01-10","id":42,"title":"knitting"},
            |{"size":42,"needleDiameter":3.5,"rating":4.5,"description":"my second knitting","started":"2018-01-11", "finished":"2018-01-12","id":43,"title":"knitting 2"}]
        """.trimMargin()
        val json = JSONArray(s)
        val knittings = json.toKnittings(ApplicationProvider.getApplicationContext()).map { it.first }
        assertEquals(2, knittings.size)
        val k = knittings[0]
        assertEquals(42, k.id)
        assertEquals("knitting", k.title)
        assertEquals("my first knitting", k.description)
        assertEquals("2018-01-10", dateFormat.format(k.started))
        assertNull(k.finished)
        assertEquals("3", k.needleDiameter)
        assertEquals("41", k.size)
        assertEquals(5.0, k.rating, 0.000001)
        val k2 = knittings[1]
        assertEquals(43, k2.id)
        assertEquals("knitting 2", k2.title)
        assertEquals("my second knitting", k2.description)
        assertEquals("2018-01-11", dateFormat.format(k2.started))
        assertEquals("2018-01-12", dateFormat.format(k2.finished))
        assertEquals("3.5", k2.needleDiameter)
        assertEquals("42", k2.size)
        assertEquals(4.5, k2.rating, 0.000001)
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
        assertEquals(43, p.ownerID)
        assertEquals("socks", p.description)
        val p2 = photos[1]
        assertEquals(43, p2.id)
        assertEquals(File("/tmp/photo2.jpg"), p2.filename)
        assertEquals(44, p2.ownerID)
        assertEquals("shirt", p2.description)
    }

    @Test
    fun testJSONArrayNeedles() {
        val s = """[{"size":"5 mm","material":"METAL","name":"needle","length":"20 cm","inUse":true,"description":"my first needle","id":42,"type":"SET"},
            |{"size":"6 mm","material":"WOOD","name":"needle2","length":"21 cm","inUse":false,"description":"my second needle","id":43,"type":"ROUND"}]""".trimMargin()
        val json = JSONArray(s)
        val needles = json.toNeedles(ApplicationProvider.getApplicationContext())
        assertEquals(2, needles.size)
        assertEquals(Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET), needles[0])
        assertEquals(Needle(43, "needle2", "my second needle", "6 mm", "21 cm", NeedleMaterial.WOOD, false, NeedleType.ROUND), needles[1])
    }

    @Test
    fun testDatabaseToJSON() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        c.set(2018, 0, 11)
        val started2 = c.time
        c.set(2018, 0, 12)
        val finished = c.time
        val k1 = Knitting(44, "knitting 2", "my second knitting", started2, finished, "3.5", "41.5", null, 4.5)
        val knittings = listOf(k0, k1)
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val p1 = Photo(43, File("/tmp/photo2.jpg"), 44, "shirt", null)
        val photos = listOf(p0, p1)
        val db = Database(knittings, photos, ArrayList(), ArrayList(), emptyList())
        val json = db.toJSON()
        assertEquals(5, json.getInt("version"))
        // get the JSON array containing the knittings
        val ks = json.getJSONArray("knittings")
        assertEquals(2, ks.length())
        val j0 = ks.getJSONObject(0)
        assertEquals(42, j0.getLong("id"))
        assertEquals("knitting", j0.getString("title"))
        assertEquals("my first knitting", j0.getString("description"))
        assertEquals("2018-01-10", j0.getString("started"))
        assertFalse(j0.has("finished"))
        assertEquals(3.0, j0.getDouble("needleDiameter"), 0.000001)
        assertEquals(41.0, j0.getDouble("size"), 0.000001)
        assertEquals(5.0, j0.getDouble("rating"), 0.000001)
        assertTrue(j0.isNull("defaultPhoto"))
        val j1 = ks.getJSONObject(1)
        assertEquals(44, j1.getLong("id"))
        assertEquals("knitting 2", j1.getString("title"))
        assertEquals("my second knitting", j1.getString("description"))
        assertEquals("2018-01-11", j1.getString("started"))
        assertEquals("2018-01-12", j1.getString("finished"))
        assertEquals(3.5, j1.getDouble("needleDiameter"), 0.000001)
        assertEquals(41.5, j1.getDouble("size"), 0.000001)
        assertEquals(4.5, j1.getDouble("rating"), 0.000001)
        assertTrue(j0.isNull("defaultPhoto"))
        val ps = json.getJSONArray("photos")
        assertEquals(2, ps.length())
        val j2 = ps.getJSONObject(0)
        assertEquals(42, j2.getLong("id"))
        assertEquals("/tmp/photo1.jpg", j2.getString("filename"))
        assertEquals(43, j2.getLong("knittingID"))
        assertEquals("socks", j2.getString("description"))
        val j3 = ps.getJSONObject(1)
        assertEquals(43, j3.getLong("id"))
        assertEquals("/tmp/photo2.jpg", j3.getString("filename"))
        assertEquals(44, j3.getLong("knittingID"))
        assertEquals("shirt", j3.getString("description"))
    }

    @Test
    fun testJSONObjectToDatabase() {
        val s = """{"knittings":[{"size":41,"needleDiameter":3,"rating":5,"description":"my first knitting","started":"2018-01-10","id":42,"title":"knitting"},
            {"size":41.5,"needleDiameter":3.5,"rating":4.5,"description":"my second knitting","started":"2018-01-11","finished":"2018-01-12","id":44,"title":"knitting 2"}],
            "photos":[{"knittingID":43,"filename":"/tmp/photo1.jpg","description":"socks","id":42},{"knittingID":44,"filename":"/tmp/photo2.jpg","description":"shirt","id":43}]}"""
        val json = JSONObject(s)
        val db = json.toDatabase(ApplicationProvider.getApplicationContext(), File("/tmp"))
        assertEquals(2, db.projects.size)
        assertEquals(2, db.photos.size)
        val k = db.projects[0]
        assertEquals(42, k.id)
        assertEquals("knitting", k.title)
        assertEquals("my first knitting", k.description)
        assertEquals("2018-01-10", dateFormat.format(k.started))
        assertNull(k.finished)
        assertEquals("3", k.needleDiameter)
        assertEquals("41", k.size)
        assertEquals(5.0, k.rating, 0.00001)
        val k2 = db.projects[1]
        assertEquals(44, k2.id)
        assertEquals("knitting 2", k2.title)
        assertEquals("my second knitting", k2.description)
        assertEquals("2018-01-11", dateFormat.format(k2.started))
        assertEquals("2018-01-12", dateFormat.format(k2.finished))
        assertEquals("3.5", k2.needleDiameter)
        assertEquals("41.5", k2.size)
        assertEquals(4.5, k2.rating, 0.00001)
        val p = db.photos[0]
        assertEquals(42, p.id)
        assertEquals(File("/tmp/photo1.jpg"), p.filename)
        assertEquals(43, p.ownerID)
        assertEquals("socks", p.description)
        val p2 = db.photos[1]
        assertEquals(43, p2.id)
        assertEquals(File("/tmp/photo2.jpg"), p2.filename)
        assertEquals(44, p2.ownerID)
        assertEquals("shirt", p2.description)
    }

    @Test
    fun testCategoryToJSON() {
        val c0 = Category(42, "test", null)
        val j0 = c0.toJSON()
        assertEquals(42, j0.getLong("id"))
        assertEquals("test", j0.getString("name"))
        val c1 = Category(43, "socks", Color.RED)
        val j1 = c1.toJSON()
        assertEquals(43, j1.getLong("id"))
        assertEquals("socks", j1.getString("name"))
        assertEquals(Color.RED, Color.parseColor(j1.getString("color")))
    }

    @Test
    fun testJSONToCategory() {
        val s1 = """{"name":"test","id":42}"""
        assertEquals(Category(42, "test", null), JSONObject(s1).toCategory())
        val s2 = """{"color":"#FFFF0000","name":"socks","id":43}"""
        assertEquals(Category(43, "socks", Color.RED), JSONObject(s2).toCategory())
    }

    @Test
    fun testJSONArrayToCategories() {
        val s = """[{"name":"test","id":42},{"color":"#FFFF0000","name":"socks","id":43}]"""
        val json = JSONArray(s)
        val categories = json.toCategories()
        assertEquals(2, categories.size)
        assertEquals(Category(42, "test", null), categories[0])
        assertEquals(Category(43, "socks", Color.RED), categories[1])
    }

    @Test
    fun testJSONToRowCounter() {
        val s = """{"id": 42, "totalRows:" 43, "rowsPerRepeat:" 2, "knittingID:" 44}"""
        val json = JSONObject(s)
        val rc = json.toRowCounter()
        assertEquals(RowCounter(42, 43, 2, 44), rc)
    }
}