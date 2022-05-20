package com.mthaler.knittings.stopwatch

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NavUtils
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentStopwatchBinding
import com.mthaler.knittings.model.Knitting

class StopwatchFragment : Fragment() {

    private var _binding: FragmentStopwatchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StopwatchViewModel
    private var knittingID: Long = Knitting.EMPTY.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(StopwatchViewModel::class.java)

        val a = arguments
        if (a != null) {
            knittingID = a.getLong(EXTRA_KNITTING_ID)
            viewModel.elapsedTime = KnittingsDataSource.getProject(knittingID).duration
        }

        setHasOptionsMenu(true)

        viewModel.runTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStopwatchBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel.time.observe(viewLifecycleOwner, { time ->
            binding.timeView.text = time
        })

        binding.startButton.setOnClickListener {
            viewModel.running = true
            viewModel.previousTime = System.currentTimeMillis()
        }

        binding.stopButton.setOnClickListener {
            viewModel.running = false
            val knitting = KnittingsDataSource.getProject(knittingID)
            KnittingsDataSource.updateProject(knitting.copy(duration = viewModel.elapsedTime))
        }

        binding.discardButton.setOnClickListener {
            viewModel.running = false
            viewModel.elapsedTime = 0
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

    companion object {
        @JvmStatic
        fun newInstance(knittinhID: Long) =
            StopwatchFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittinhID)
                }
            }
    }
}