package com.mthaler.knittings.category

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before

class CategoryAdapterTest {

    private lateinit var context: Context
    private lateinit var adapter: CategoryAdapter

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        adapter = CategoryAdapter({ c -> {} }, { c -> {} })
    }
}