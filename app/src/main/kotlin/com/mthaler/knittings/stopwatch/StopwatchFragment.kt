package com.mthaler.knittings.stopwatch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NavUtils
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentStopwatchBinding
import com.mthaler.knittings.model.Knitting
import java.util.*

class StopwatchFragment : Fragment() {

    private var _binding: FragmentStopwatchBinding? = null
    private val binding get() = _binding!!
    private var knittingID: Long = Knitting.EMPTY.id

    private var elapsedTime = 0L
    private var previousTime = 0L
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val a = arguments
        if (a != null) {
            knittingID = a.getLong(EXTRA_KNITTING_ID)
        }


        if (savedInstanceState != null) {
            val t = savedInstanceState.getLong(EXTRA_STOPWATCH_ACTIVITY_STOPPED_TIME)
            elapsedTime = savedInstanceState.getLong(EXTRA_STOPWATCH_ELAPSED_TIME) + (System.currentTimeMillis() - t)
            running = savedInstanceState.getBoolean(EXTRA_STOPWATCH_RUNNING)
        }

        setHasOptionsMenu(true)

        runTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStopwatchBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.startButton.setOnClickListener {
            running = true
            previousTime = System.currentTimeMillis()
        }

        binding.stopButton.setOnClickListener {
            running = false
            val knitting = KnittingsDataSource.getProject(knittingID)
            KnittingsDataSource.updateProject(knitting.copy(duration = elapsedTime))
        }

        binding.discardButton.setOnClickListener {
            running = false
            elapsedTime = 0
            requireActivity().finish()
        }

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(requireActivity())
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                upIntent.putExtra(EXTRA_KNITTING_ID, knittingID)
                NavUtils.navigateUpTo(requireActivity(), upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

      private fun runTimer() {
        previousTime = System.currentTimeMillis()
        val handler = Handler(Looper.myLooper()!!)
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


    companion object {
        const val EXTRA_STOPWATCH_RUNNING = "com.mthaler.knitting.STOPWATCH_RUNNING"
        const val EXTRA_STOPWATCH_ELAPSED_TIME = "com.mthaler.knitting.STOPWATCH_ELAPSED_TIME"
        const val EXTRA_STOPWATCH_ACTIVITY_STOPPED_TIME = "com.mthaler.knitting.STOPWATCH_ACTIVITY_STOPPED_TIME"

        @JvmStatic
        fun newInstance(knittinhID: Long) =
            StopwatchFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittinhID)
                }
            }
    }
}