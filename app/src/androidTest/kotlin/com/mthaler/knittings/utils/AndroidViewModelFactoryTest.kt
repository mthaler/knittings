package com.mthaler.knittings.utils

import android.app.Application
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mthaler.knittings.MainActivity
import com.mthaler.knittings.MainViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import junit.framework.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class AndroidViewModelFactoryTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        launchActivity<MainActivity>().use { scenario ->
            scenario.onActivity { activity ->
                application = activity.application
            }
        }
    }

    @Test
    fun testCreate() {
        val f = AndroidViewModelFactory(application)
        val i0 = f.create(MainViewModel::class.java)
        assertNotNull(i0)
    }
}