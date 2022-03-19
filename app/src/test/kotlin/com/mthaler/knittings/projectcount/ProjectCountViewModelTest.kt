package com.mthaler.knittings.projectcount

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.projectcount.ProjectCountViewModel
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.util.*

class ProjectCountViewModelTest {

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