package com.mthaler.knittings.category

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.model.Category
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryAdapterTest {

    private lateinit var context: Context
    private lateinit var adapter: CategoryAdapter

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        adapter = CategoryAdapter({ c -> {} }, { c -> {} })
    }

    @Test
    fun itemCount() {
        val c0 = Category(42, "test", null)
        val categories = ArrayList<Category>()
        categories.add(c0)
        adapter.setCategories(categories)
        assertEquals(1, adapter.itemCount)
    }
}