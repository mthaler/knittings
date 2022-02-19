package com.mthaler.knittings.compressphotos

import android.util.Log
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.hamcrest.core.Is.`is`
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompressPhotoTest {

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    @Throws(Exception::class)
    fun testEchoWorkerNoInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
       // Create request
       val request = OneTimeWorkRequestBuilder<EchoWorker>()
           .build()

       val workManager = WorkManager.getInstance(context)
       // Enqueue and wait for result. This also runs the Worker synchronously
       // because we are using a SynchronousExecutor.
       workManager.enqueue(request).result.get()
       // Get WorkInfo
       val workInfo = workManager.getWorkInfoById(request.id).get()

       // Assert
       assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

}