package com.mthaler.knittings.database

import android.graphics.Color
import com.mthaler.knittings.database.table.CategoryTable
import com.mthaler.knittings.model.Category
import junit.framework.TestCase
import org.junit.Test

class CategoryConverterTest {

    @Test
    fun testConvert() {
        val dbRows = mapOf(CategoryTable.Cols.ID to 42L, CategoryTable.Cols.NAME to "test", CategoryTable.Cols.COLOR to Color.RED.toLong())
        val result = CategoryConverter.convert(dbRows)
        val c = Category(42, "test", Color.RED)
        TestCase.assertEquals(c, result)
    }
}