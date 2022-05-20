package com.mthaler.knittings.needle

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NeedleListViewModelTest {

    companion object {
        val DATABASE_NAME = "knittings_test.db"
    }

    @get: Rule
    val schedulers = InstantTaskExecutorRule()

    private lateinit var mDatabase: SQLiteDatabase
    private lateinit var viewModel: NeedleListViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        mDatabase = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)
        viewModel = NeedleListViewModel()
    }

    @Test
    fun testIsLiveDataEmitting() {
        val n0 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, true, NeedleType.ROUND)
        val n1 = Needle(42, "needle", "my first needle", "5 mm", "20 cm", NeedleMaterial.METAL, false, NeedleType.CIRCULAR)
        viewModel._needles.value = listOf(n0, n1)
        assertEquals(viewModel.needles.value, listOf(n0, n1))
    }
}