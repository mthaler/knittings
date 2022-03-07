package com.mthaler.knittings.category

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.MyApplication
import com.mthaler.knittings.model.Knitting
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryListViewModelTest {

    lateinit var application: DatabaseApplication

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        application = instrumentation.newApplication(ClassLoader.getSystemClassLoader(), "com.mthaler.knittings.MyApplication", context) as MyApplication
    }

    @Test()
    fun testCategorie() {
        val vm = CategoryListViewModel(application as Application)
        val categories = vm.categories
    }
}