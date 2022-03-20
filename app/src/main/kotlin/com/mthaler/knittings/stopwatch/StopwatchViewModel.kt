package com.mthaler.knittings.stopwatch

import android.os.Handler

class StopwatchViewModel {

    private var elapsedTime = 0L
    private var previousTime = 0L
    private var running = false

    fun runTimer() {
        previousTime = System.currentTimeMillis()
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val totalSeconds = elapsedTime / 1000
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