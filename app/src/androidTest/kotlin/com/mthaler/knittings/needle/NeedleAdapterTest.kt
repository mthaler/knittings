package com.mthaler.knittings.needle

import android.app.LauncherActivity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.model.Knitting
import junit.framework.Assert.assertEquals
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import java.util.*
import kotlin.collections.ArrayList

@RunWith(AndroidJUnit4::class)
class NeedleAdapterTest {

    private lateinit var context: Context
    private lateinit var adapter: NeedleAdapter
    private lateinit var mockView: View
    private lateinit var mockFragment: Fragment

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        adapter = NeedleAdapter({ n -> {} }, { n -> {} })
        mockView = mock(View::class.java)
        mockFragment = mock(Fragment::class.java)
    }

    @Test
    fun itemCount() {
        adapter.setItems(emptyList())
        assertEquals(0, adapter.itemCount)
    }
}