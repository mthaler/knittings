package com.mthaler.knittings.compressphotos

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompressPhotoFragmentTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testCompressPhotosFragmentRecreate() {
        val scenario = launchFragmentInContainer<CompressPhotoFragment>()
        scenario.recreate()
    }
}