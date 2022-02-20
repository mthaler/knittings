package com.mthaler.knittings.needle.filter

import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SingleTypeFilterTest {

    @Test
    fun filter() {
        val f = SingleTypeFilter(NeedleType.ROUND)
        val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.ROUND)
        val n1 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.CIRCULAR)
        val filtered = f.filter(listOf(n0, n1))
        assertEquals(listOf(n0), filtered)
    }
}