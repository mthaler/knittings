package com.mthaler.knittings.compressphotos

import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompressPhotosActivityTest {

    @Test
    fun testRecreate() {
        val scenario = launchActivity<CompressPhotosActivity>()
        scenario.recreate()
    }
}