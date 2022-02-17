package com.mthaler.knittings.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class KnittingsDataSourceTest {

    @Before
    fun initDatabase() {
        deleteAllKnittings()
    }

    @After
    fun cleanDatabase() {
        deleteAllKnittings()
    }

    @Test
    fun testAddKnitting() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3 = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        val knittings: ArrayList<Knitting> = ArrayList<Knitting>()
        knittings.add(knitting1)
        knittings.add(knitting3)
        knittings.add(knitting5)
        Assert.assertEquals(knittings, ds.allProjects)
    }

    @Test
    fun testDeleteKnitting() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().context
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3 = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        ds.deleteProject(knitting3)
        Assert.assertEquals(2, ds.allProjects.size.toLong())
    }

    @Test
    fun testUpdateKnitting() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().context
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3 = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        val updated: Knitting = knitting3.copy(knitting3.id, knitting3.title, "Updated knitting", knitting3.started, knitting3.finished, knitting3.needleDiameter,
            knitting3.size, knitting3.defaultPhoto, knitting3.rating, knitting3.duration, knitting3.category)
        val result: Knitting = ds.updateProject(updated)
        Assert.assertEquals(updated, result)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        val knittings = ArrayList<Knitting>()
        knittings.add(knitting1)
        knittings.add(updated)
        knittings.add(knitting5)
        Assert.assertEquals(knittings, ds.allProjects)
    }

    @Test
    fun testGetKnitting() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().context
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3 = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        Assert.assertEquals(knitting1, ds.getProject(knitting1.id))
        Assert.assertEquals(knitting3, ds.getProject(knitting3.id))
        Assert.assertEquals(knitting5, ds.getProject(knitting5.id))
    }

    @Test
    fun testCreatePhoto() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().context
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3 = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        Assert.assertTrue(ds.allPhotos.isEmpty())
        val photo0 = Photo(-1, File("/path/to/photo"), knitting3.id)
        val photo1: Photo = ds.addPhoto(photo0)
        Assert.assertEquals(1, ds.allPhotos.size.toLong())
        val photos = ArrayList<Photo>()
        photos.add(photo1)
        Assert.assertEquals(photos, ds.allPhotos)
        ds.deleteProject(knitting3)
        Assert.assertTrue(ds.allPhotos.isEmpty())
    }

    @Test
    fun testUpdatePhoto() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().context
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3 = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        Assert.assertTrue(ds.allPhotos.isEmpty())
        val photo0 = Photo(-1, File("/path/to/photo"), knitting3.id)
        val photo1: Photo = ds.addPhoto(photo0)
        Assert.assertEquals(1, ds.allPhotos.size.toLong())
        val photos = ArrayList<Photo>()
        photos.add(photo1)
        Assert.assertEquals(photos, ds.allPhotos)
        val updated = photo1.copy(photo1.id, photo1.filename, photo1.ownerID, "updated description", photo1.preview)
        ds.updatePhoto(updated)
        val photos2 = ArrayList<Photo>()
        photos2.add(updated)
        Assert.assertEquals(photos2, ds.allPhotos)
    }

    @Test
    fun testGetPhoto() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().context
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3: Knitting = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        Assert.assertTrue(ds.allPhotos.isEmpty())
        val photo0 = Photo(-1, File("/path/to/photo"), knitting3.id)
        val photo1: Photo = ds.addPhoto(photo0)
        Assert.assertEquals(1, ds.allPhotos.size.toLong())
        val result = ds.getPhoto(photo1.id)
        Assert.assertEquals(photo1, result)
    }

    private fun deleteAllKnittings() {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().context
        val ds = KnittingsDataSource
        ds.init(ctx)
        Assert.assertTrue(ds.allProjects.isEmpty())
        val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0")
        val knitting1 = ds.addProject(knitting0)
        val knitting2 = Knitting(-1, "test knitting 2", "second test knitting", Date(), null, "4.0", "43.0")
        val knitting3: Knitting = ds.addProject(knitting2)
        val knitting4 = Knitting(-1, "test knitting 3", "third test knitting", Date(), null, "2.0", "39.0")
        val knitting5 = ds.addProject(knitting4)
        Assert.assertEquals(3, ds.allProjects.size.toLong())
        ds.deleteAllProjects()
        Assert.assertEquals(0, ds.allProjects.size.toLong())
        val db = ctx.databaseList()[0]
    }
}