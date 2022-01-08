package com.mthaler.knittings.stopwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.app.NavUtils
import android.view.MenuItem
import android.view.View
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.ActivityStopwatchBinding
import java.util.Locale

/**
 * StopWatchActivity shows a stopwatch that can be used to measure the time the user is working on a knitting
 */
class   StopwatchActivity : BaseActivity() {

    private lateinit var binding: ActivityStopwatchBinding
    private var knittingID: Long = -1
    private var elapsedTime = 0L
    private var previousTime = 0L
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopwatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the knitting for the stopwatch
        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            knittingID = id
            elapsedTime = KnittingsDataSource.getProject(knittingID).duration
        } else {
            error("Could not get knitting id")
        }
        if (savedInstanceState != null) {
            val t = savedInstanceState.getLong(EXTRA_STOPWATCH_ACTIVITY_STOPPED_TIME)
            elapsedTime = savedInstanceState.getLong(EXTRA_STOPWATCH_ELAPSED_TIME) + (System.currentTimeMillis() - t)
            running = savedInstanceState.getBoolean(EXTRA_STOPWATCH_RUNNING)
        }
        runTimer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        outState.putLong(EXTRA_STOPWATCH_ELAPSED_TIME, elapsedTime)
        outState.putLong(EXTRA_STOPWATCH_ACTIVITY_STOPPED_TIME, System.currentTimeMillis())
        outState.putBoolean(EXTRA_STOPWATCH_RUNNING, running)
        super.onSaveInstanceState(outState)
    }

    // Start the stopwatch running when the Start button is clicked.
    fun onClickStart(view: View) {
        running = true
        previousTime = System.currentTimeMillis()
    }

    // Stop the stopwatch running when the Stop button is clicked.
    fun onClickStop(view: View) {
        running = false
        val knitting = KnittingsDataSource.getProject(knittingID)
        KnittingsDataSource.updateProject(knitting.copy(duration = elapsedTime))
    }

    fun onClickDiscard(view: View) {
        running = false
        elapsedTime = 0
        finish()
    }

    private fun runTimer() {
        previousTime = System.currentTimeMillis()
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val totalSeconds = elapsedTime / 1000
                val hours = totalSeconds / 3600
                val minutes = totalSeconds % 3600 / 60
                val secs = totalSeconds % 60
                val time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                binding.timeView.text = time
                if (running) {
                    val t = System.currentTimeMillis()
                    elapsedTime += (t - previousTime)
                    previousTime = t
                }
                handler.postDelayed(this, 500)
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
        const val EXTRA_STOPWATCH_RUNNING = "com.mthaler.knitting.STOPWATCH_RUNNING"
        const val EXTRA_STOPWATCH_ELAPSED_TIME = "com.mthaler.knitting.STOPWATCH_ELAPSED_TIME"
        const val EXTRA_STOPWATCH_ACTIVITY_STOPPED_TIME = "com.mthaler.knitting.STOPWATCH_ACTIVITY_STOPPED_TIME"

        fun newIntent(context: Context, knittingID: Long): Intent {
            val intent = Intent(context, StopwatchActivity::class.java)
            intent.putExtra(EXTRA_KNITTING_ID, knittingID)
            return intent
        }
    }
}
