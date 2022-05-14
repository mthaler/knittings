package com.mthaler.knittings.stopwatch

import androidx.lifecycle.Observer
import org.junit.Test
import junit.framework.TestCase.assertEquals
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class StopwatchViewModelTest {

    @Test
    fun time() {
        var t = ""
        val latch = CountDownLatch(1)
        val s = StopwatchViewModel()
        val observer = object : Observer<String> {
            override fun onChanged(receivedTime: String?) {
                t = receivedTime!!
                latch.countDown()
                s.time.removeObserver(this)
            }
        }
        s.time.observeForever(observer)
        s.timer.cancel()
        latch.await(10, TimeUnit.SECONDS)
        assertEquals("0:00:00", t)
    }

}