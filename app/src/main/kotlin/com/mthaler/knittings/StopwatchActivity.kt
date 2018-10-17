package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import com.mthaler.knittings.database.datasource
import java.util.*

class StopwatchActivity : AppCompatActivity() {

    private var knittingID: Long = -1
    private var seconds = 0L
    private var running = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)
        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            knittingID = id
            seconds = datasource.getKnitting(knittingID).duration
        } else {
            error("Could not get knitting id")
        }
        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean("running")
        }
        runTimer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        outState.putBoolean("running", running)
        super.onSaveInstanceState(outState)
    }

    //Start the stopwatch running when the Start button is clicked.
    fun onClickStart(view: View) {
        running = true
    }

    //Stop the stopwatch running when the Stop button is clicked.
    fun onClickStop(view: View) {
        running = false
        val knitting = datasource.getKnitting(knittingID)
        datasource.updateKnitting(knitting.copy(duration = seconds))

    }

    fun onClickReset(view: View) {
        running = false
        seconds = 0
    }

    private fun runTimer() {
        val timeView = findViewById<TextView>(R.id.time_view)
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = seconds % 3600 / 60
                val secs = seconds % 60
                val time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                timeView.setText(time)
                if (running) {
                    seconds++
                }
                handler.postDelayed(this, 1000)

            }
        })
    }

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
