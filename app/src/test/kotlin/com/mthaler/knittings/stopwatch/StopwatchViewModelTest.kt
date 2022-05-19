package com.mthaler.knittings.stopwatch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StopwatchViewModelTest {

    @get:Rule
    val schedulers = InstantTaskExecutorRule()

    lateinit var viewModel: StopwatchViewModel

    @Before
    fun setUp() {
        viewModel = StopwatchViewModel()
    }

    @Test
    fun testIsLiveDataEmitting() {
        viewModel._time.value = "foo"
        assertEquals(viewModel.time.value, "foo") // Passes
    }

    @Test
    fun testTime() {
        viewModel.start()
        viewModel.time.test()
            .assertHasValue()
            .assertValue("0:00:00")
    }

}