package com.mthaler.knittings.stopwatch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Knitting
import java.util.*
import com.mthaler.knittings.utils.setMutVal

class StopwatchViewModel: ViewModel() {

    var knittingID: Long = Knitting.EMPTY.id

    var elapsedTime = 0L
    var previousTime = 0L
    var running = false
    lateinit var timer: Timer

    fun start() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val totalSeconds = elapsedTime / 1000
                val hours = totalSeconds / 3600
                val minutes = totalSeconds % 3600 / 60
                val secs = totalSeconds % 60
                time.setMutVal(String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs))
                if (running) {
                    val t = System.currentTimeMillis()
                    elapsedTime += (t - previousTime)
                    previousTime = t
                }
            }
        }, 500, 500)
    }

    val time = MutableLiveData("")
}