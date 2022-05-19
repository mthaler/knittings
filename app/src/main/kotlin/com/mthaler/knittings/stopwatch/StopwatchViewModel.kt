package com.mthaler.knittings.stopwatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mthaler.knittings.model.Knitting
import java.util.*
import com.mthaler.knittings.utils.setMutVal

class StopwatchViewModel: ViewModel() {

    var knittingID: Long = Knitting.EMPTY.id

    var elapsedTime = 0L
    lateinit var timer: Timer
    val _time = MutableLiveData("")

    fun start() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val totalSeconds = elapsedTime / 1000
                val hours = totalSeconds / 3600
                val minutes = totalSeconds % 3600 / 60
                val secs = totalSeconds % 60
                val s = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                if (s != "") {
                    _time.value = s
                }
            }
        }, 0, 500)
    }

    fun stop() {
        timer.cancel()
    }

    val time: LiveData<String> = _time

    fun setNewValue(newValue: String) {
        _time.setMutVal(newValue)
    }
}