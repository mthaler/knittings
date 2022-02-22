package com.mthaler.knittings.needle

import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NeedleListActivityTest {

    @Test
    fun testRecreate() {
          val scenario = launchActivity<NeedleListActivity>()
          scenario.recreate()
    }
}