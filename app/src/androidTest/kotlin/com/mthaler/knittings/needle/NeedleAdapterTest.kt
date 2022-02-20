package com.mthaler.knittings.needle

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

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
}