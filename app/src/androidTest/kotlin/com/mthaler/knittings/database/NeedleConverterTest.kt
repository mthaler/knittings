package com.mthaler.knittings.database

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.database.table.NeedleTable
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NeedleConverterTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testConvert() {
        val dbRows = mapOf(NeedleTable.Cols.ID to 42, NeedleTable.Cols.NAME to "needle", NeedleTable.Cols.DESCRIPTION to "my first needle", NeedleTable.Cols.SIZE to "5 mm", NeedleTable.Cols.LENGTH to "20 cm",
            NeedleTable.Cols.MATERIAL to  NeedleMaterial.METAL.toString(), NeedleTable.Cols.IN_USE to true, NeedleTable.Cols.TYPE to NeedleType.SET.toString())
        val result = NeedleConverter(context).convert(dbRows)
        val n = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET)
        assertEquals(result, n)
    }
}