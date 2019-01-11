package com.mthaler.knittings.stopwatch

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.NavUtils
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import java.util.*

/**
 * StopWatchActivity shows a stopwatch that can be used to measure the time the user is working on a knitting
 */
class StopwatchActivity : AppCompatActivity() {

    private var knittingID: Long = -1
    private var seconds = 0L
    private var running = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        // get the knitting for the stopwatch
        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            knittingID = id
            seconds = datasource.getKnitting(knittingID).duration / 1000
        } else {
            error("Could not get knitting id")
        }
        if (savedInstanceState != null) {
            seconds = savedInstanceState.getLong("seconds")
            running = savedInstanceState.getBoolean(EXTRA_STOPWATCH_RUNNING)
        }
        runTimer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        outState.putLong("seconds", seconds)
        outState.putBoolean(EXTRA_STOPWATCH_RUNNING, running)
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
        datasource.updateKnitting(knitting.copy(duration = seconds * 1000))

    }

    fun onClickDiscard(view: View) {
        running = false
        seconds = 0
        finish()
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
                timeView.text = time
                if (running) {
                    seconds++
                }
                handler.postDelayed(this, 1000)

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                upIntent.putExtra(EXTRA_KNITTING_ID, knittingID)
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        val EXTRA_STOPWATCH_RUNNING = "com.mthaler.knitting.STOPWATCH_RUNNING"
    }
}
