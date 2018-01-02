package com.mthaler.knittings.database

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class KnittingsDataSourceTest {

    @Test
    @Throws(Exception::class)
    fun testCreateKnitting() {
        val ctx = InstrumentationRegistry.getTargetContext()
        val ds = KnittingsDataSource.getInstance(ctx)
        assertTrue(ds.allKnittings.isEmpty())
    }
}