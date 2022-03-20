package com.mthaler.knittings.stopwatch

import android.os.Handler
import androidx.lifecycle.ViewModel

class StopwatchViewModel: ViewModel() {

    var elapsedTime = 0L
    var previousTime = 0L
    var running = false

    fun runTimer() {
        previousTime = System.currentTimeMillis()
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                if (running) {
                    val t = System.currentTimeMillis()
                    elapsedTime += (t - previousTime)
                    previousTime = t
                }
                handler.postDelayed(this, 500)
            }
        })
    }
}