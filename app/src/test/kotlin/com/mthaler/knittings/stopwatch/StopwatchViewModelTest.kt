package com.mthaler.knittings.stopwatch

import com.jraska.livedata.test
import org.junit.Test

class StopwatchViewModelTest {

    @Test
    fun time() {
        val s = StopwatchViewModel()
        s.start()
        s.time.test()
            .assertHasValue()
            .assertValue("0:00:00")
    }

}