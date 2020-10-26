package com.mthaler.knittings.rowcounter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Knitting

class RowCounterFragment : Fragment() {

    private var knittingID: Long = Knitting.EMPTY.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Extras.EXTRA_KNITTING_ID)) {
                knittingID = savedInstanceState.getLong(Extras.EXTRA_KNITTING_ID)
            }
        }

        val v = inflater.inflate(R.layout.fragment_row_counter, container, false)

        val textViewRows = v.findViewById<TextView>(R.id.rows)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(RowCounterViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.rows.observe(viewLifecycleOwner, Observer { rows ->
            textViewRows.text = rows.totalRows.toString()
        })

        val plusButton = v.findViewById<ImageButton>(R.id.button_plus)
        plusButton.setOnClickListener {
            viewModel.incrementTotalRows()
        }

        val minusButton = v.findViewById<ImageButton>(R.id.button_minus)
        minusButton.setOnClickListener {
            viewModel.decrementTotalRows()
        }

        val resetButton = v.findViewById<ImageButton>(R.id.button_reset)
        resetButton.setOnClickListener {
            ResetRowCounterDialog.create(requireContext(), { viewModel.clearTotalRows() }).show()
        }

        // Inflate the layout for this fragment
        return v
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(Extras.EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(outState)
    }

    companion object {

        @JvmStatic
        fun newInstance(knittingID: Long) =
                RowCounterFragment().apply {
                    arguments = Bundle().apply {
                        putLong(Extras.EXTRA_KNITTING_ID, knittingID)
                    }
                }
    }
}