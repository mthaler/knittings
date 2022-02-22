package com.mthaler.knittings.dropbox

import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DropboxExportActivityTest {

    @Test
    fun testDropboxRecreate() {
          val scenario = launchActivity<DropboxExportActivity>()
          scenario.recreate()
    }
}