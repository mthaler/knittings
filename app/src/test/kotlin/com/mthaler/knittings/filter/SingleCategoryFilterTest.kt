package com.mthaler.knittings.filter

import android.graphics.Color
import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Knitting
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.util.*

class SingleCategoryFilterTest {

     @Test
     fun filter() {
          val c0 = Category(42, "test", Color.RED)
          val c1 = Category(43, "test2", Color.BLUE)
          val f = SingleCategoryFilter<Knitting>(c0)
          val knitting0 = Knitting(-1,"test knitting 1", "first test knitting", Date(), null, "3.0", "42.0", null, 4.0, 0L, c0)
          val knitting1 = Knitting(-1,"test knitting 2", "second test knitting", Date(), null, "3.0", "42.0", null, 4.0, 0L, c1)
          val filtered = f.filter(listOf(knitting0, knitting1))
          assertEquals(listOf(knitting0), filtered)
     }
}