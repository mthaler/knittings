package com.mthaler.knittings.needle

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import junit.framework.Assert.assertEquals
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class NeedleAdapterTest {

    private lateinit var context: Context
    private lateinit var adapter: NeedleAdapter

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        adapter = NeedleAdapter({ n -> {} }, { n -> {} })
    }

    @Test
    fun itemCount() {
        val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET)
        adapter.setItems(emptyList())
        assertEquals(0, adapter.itemCount)
    }
}