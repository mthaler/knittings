package com.mthaler.knittings.model

import com.mthaler.knittings.utils.SerializeUtils
import org.junit.Assert.*
import org.junit.Test

class NeedleTest {

    @Test
    fun testSerializeDeserialize() {
        val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET)
        val n1 = SerializeUtils.deserialize<Needle>(SerializeUtils.serialize(n0))
        assertEquals(n0, n1)
    }
}