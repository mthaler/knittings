package com.mthaler.knittings.compressphotos

import android.content.Context
import android.util.Log
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.hamcrest.core.Is.`is`
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CompressPhotoTest {

    companion object {
        const val KEY_1 = "key1"
        const val KEY_2 = "key2"
    }

    lateinit var context: Context
    lateinit var executor: Executor

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        executor = Executors.newSingleThreadExecutor()

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
       // Create request
       val request = OneTimeWorkRequestBuilder<EchoWorker>().build()

       val workManager = WorkManager.getInstance(context)
       // Enqueue and wait for result. This also runs the Worker synchronously
       // because we are using a SynchronousExecutor.
       workManager.enqueue(request).result.get()
       // Get WorkInfo
       val workInfo = workManager.getWorkInfoById(request.id).get()

       // Assert
       assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    @Test
    @Throws(Exception::class)
    fun testWithInitialDelay() {
        // Define input data
        val input = workDataOf(KEY_1 to 1, KEY_2 to 2)

        // Create request
        val request = OneTimeWorkRequestBuilder<EchoWorker>()
            .setInputData(input)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        val workManager = WorkManager.getInstance(context)
        // Get the TestDriver
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        // Enqueue
        workManager.enqueue(request).result.get()
        // Tells the WorkManager test framework that initial delays are now met.
        testDriver?.setInitialDelayMet(request.id)
        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()
        val outputData = workInfo.outputData

        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
        assertThat(outputData, `is`(input))
    }

    @Test
    @Throws(Exception::class)
    fun testWithConstraints() {
        // Define input data
        val input = workDataOf(KEY_1 to 1, KEY_2 to 2)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create request
        val request = OneTimeWorkRequestBuilder<EchoWorker>()
            .setInputData(input)
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        // Enqueue
        workManager.enqueue(request).result.get()
        // Tells the testing framework that all constraints are met.
        testDriver?.setAllConstraintsMet(request.id)
        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()
        val outputData = workInfo.outputData

        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
        assertThat(outputData, `is`(input))
    }

    @Test
    @Throws(Exception::class)
    fun testPeriodicWork() {
        // Define input data
        val input = workDataOf(KEY_1 to 1, KEY_2 to 2)

        // Create request
        val request = PeriodicWorkRequestBuilder<EchoWorker>(15, TimeUnit.MINUTES)
            .setInputData(input)
            .build()

        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        // Enqueue and wait for result.
        workManager.enqueue(request).result.get()
        // Tells the testing framework the period delay is met
        testDriver?.setPeriodDelayMet(request.id)
        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()

        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }
}