package com.mthaler.knittings.stopwatch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.Extras
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentStopwatchBinding

class StopwatchFragment : Fragment() {

    private var _binding: FragmentStopwatchBinding? = null
    private val binding get() = _binding!!

    private var knittingID: Long = -1
    private var elapsedTime = 0L
    private var previousTime = 0L
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val a = arguments
        if (a != null) {
            knittingID = a.getLong(Extras.EXTRA_NEEDLE_ID)
            elapsedTime = KnittingsDataSource.getProject(knittingID).duration
        } else {
            error("Could not get knitting id")
        }
        if (savedInstanceState != null) {
            val t = savedInstanceState.getLong(StopwatchActivity.EXTRA_STOPWATCH_ACTIVITY_STOPPED_TIME)
            elapsedTime = savedInstanceState.getLong(StopwatchActivity.EXTRA_STOPWATCH_ELAPSED_TIME) + (System.currentTimeMillis() - t)
            running = savedInstanceState.getBoolean(StopwatchActivity.EXTRA_STOPWATCH_RUNNING)
        } else {
            error("Saved instance state is null")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
          _binding = FragmentStopwatchBinding.inflate(inflater, container, false)
          val view = binding.root

          binding.startButton.setOnClickListener {
              running = true
              previousTime = System.currentTimeMillis()
          }

         return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(Extras.EXTRA_KNITTING_ID, knittingID)
        outState.putLong(StopwatchActivity.EXTRA_STOPWATCH_ELAPSED_TIME, elapsedTime)
        outState.putLong(StopwatchActivity.EXTRA_STOPWATCH_ACTIVITY_STOPPED_TIME, System.currentTimeMillis())
        outState.putBoolean(StopwatchActivity.EXTRA_STOPWATCH_RUNNING, running)
        super.onSaveInstanceState(outState)
    }
}