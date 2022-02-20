package com.mthaler.knittings.needle

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import com.mthaler.knittings.needle.NeedleAdapter.Companion.TypeHeader
import com.mthaler.knittings.needle.NeedleAdapter.Companion.TypeItem
import junit.framework.Assert.assertEquals
import org.junit.*
import org.junit.runner.RunWith
import kotlin.collections.ArrayList

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
        val needles = ArrayList<Needle>();
        needles.add(n0)
        val items = NeedleAdapter.groupItems(context, needles)
        adapter.setItems(items)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun getItemViewType() {
        adapter.setItems(emptyList())
        assertEquals(0, adapter.itemCount)
         val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.SET)
        val needles = ArrayList<Needle>();
        needles.add(n0)
           val items = NeedleAdapter.groupItems(context, needles)
        adapter.setItems(items)
        assertEquals(2, adapter.itemCount)
        assertEquals(TypeHeader, adapter.getItemViewType(0))
        assertEquals(TypeItem, adapter.getItemViewType(1))
    }
}