package com.mthaler.knittings

import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class KnittingsStartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
}