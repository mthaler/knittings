package com.mthaler.knittings.details

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.model.Knitting
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class KnittingDetailsViewModelTest {

    companion object {
        val DATABASE_NAME = "knittings_test.db"
    }

    @get: Rule
    val schedulers = InstantTaskExecutorRule()

    private lateinit var mDatabase: SQLiteDatabase
    private lateinit var viewModel: KnittingDetailsViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        mDatabase = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)
        viewModel = KnittingDetailsViewModel()
    }

    @Test
    fun testIsLiveDataEmitting() {
      val c = GregorianCalendar()
        c.set(2018, 0, 10)
        val started = c.time
        val k0 = Knitting(42, "knitting", "my first knitting", started, null, "3.0", "41.0", null, 5.0)
        viewModel._knitting.value = k0
        assertEquals(viewModel.knitting.value, k0)
    }
}