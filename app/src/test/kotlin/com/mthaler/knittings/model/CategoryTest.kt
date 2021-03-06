package com.mthaler.knittings.model

import android.graphics.Color
import com.mthaler.knittings.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test

class CategoryTest {

    @Test
    fun testSerializeDeserialize() {
        val c0 = Category(42, "test", null)
        val c1 = SerializeUtils.deserialize<Category>(SerializeUtils.serialize(c0))
        assertEquals(c0, c1)
        val c2 = Category(43, "test2", Color.RED)
        val c3 = SerializeUtils.deserialize<Category>(SerializeUtils.serialize(c2))
        assertEquals(c2, c3)
    }
}