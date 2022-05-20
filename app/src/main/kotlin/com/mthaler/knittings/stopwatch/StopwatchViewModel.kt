package com.mthaler.knittings.stopwatch

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mthaler.knittings.model.Knitting
import java.util.*
import com.mthaler.knittings.utils.setMutVal

class StopwatchViewModel: ViewModel() {

    var elapsedTime = 0L
    var previousTime = 0L
    var running = false
    val _time = MutableLiveData("")

    fun runTimer() {
        previousTime = System.currentTimeMillis()
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val totalSeconds = elapsedTime / 1000
                val hours = totalSeconds / 3600
                val minutes = totalSeconds % 3600 / 60
                val secs = totalSeconds % 60
                _time.postValue(String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs))
                if (running) {
                    val t = System.currentTimeMillis()
                    elapsedTime += (t - previousTime)
                    previousTime = t
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    val time: LiveData<String> = _time
}