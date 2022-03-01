package com.mthaler.knittings.database

import com.mthaler.knittings.database.table.PhotoTable
import com.mthaler.knittings.model.Photo
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File

class PhotoConverterTest {

    @Test
    fun testConvert() {
        val dbRows = mapOf(PhotoTable.Cols.ID to 42L, PhotoTable.Cols.FILENAME to "/tmp/photo1.jpg", PhotoTable.Cols.KNITTING_ID to 43L, PhotoTable.Cols.DESCRIPTION to "socks", PhotoTable.Cols.PREVIEW to null)
        val result = PhotoConverter.convert(dbRows)
        val p = Photo(42, File("/tmp/photo1.jpg"), 43, "socks", null)
        assertEquals(p, result)
    }
}