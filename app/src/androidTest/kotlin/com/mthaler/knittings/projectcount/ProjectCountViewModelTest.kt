package com.mthaler.knittings.projectcount

import android.app.Application
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
class ProjectCountViewModelTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        val s = launchActivity<ProjectCountActivity>().use {}
        
    }

    @Test
    fun testGetProjectCount() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        c.set(2018, 0, 11)
        val finished = c.time
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val k1 = Knitting(42, "knitting", "my first knitting", started, finished, "3.0", "41.0", p0, 5.0)
        val viewModel = ProjectCountViewModel()
        val expected = viewModel.getProjectCount(listOf(k0, k1),2018, null)
        assertEquals(expected, 1)
    }

     @Test
     fun testCreateYearsList() {
        val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        c.set(2018, 0, 11)
        val finished = c.time
        val p0 = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        val k1 = Knitting(42, "knitting", "my first knitting", started, finished, "3.0", "41.0", p0, 5.0)
        val viewModel = ProjectCountViewModel()
        val expected = viewModel.createYearsList(listOf(k0, k1))
        assertEquals(expected, listOf("2022", "2021", "2020", "2019", "2018"))
     }
}