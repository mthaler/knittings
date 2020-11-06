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
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.RowCounter

class RowCounterFragment : Fragment() {

    private var knittingID: Long = Knitting.EMPTY.id
    private var rowCounter: RowCounter = RowCounter.EMPTY
    private lateinit var textViewRows: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val knitting = KnittingsDataSource.getProject(knittingID)
        val rc = KnittingsDataSource.getRowCounter(knitting)
        rowCounter = if (rc != null) rc else KnittingsDataSource.addRowCounter(RowCounter(-1, 0, 1, knittingID))
        
        val v = inflater.inflate(R.layout.fragment_row_counter, container, false)

        textViewRows = v.findViewById<TextView>(R.id.rows)
        textViewRows.text = rowCounter.totalRows.toString()

        val plusButton = v.findViewById<ImageButton>(R.id.button_plus)
        plusButton.setOnClickListener {
            updateTotoalRows { it + 1 }

        }

        val minusButton = v.findViewById<ImageButton>(R.id.button_minus)
        minusButton.setOnClickListener {
            updateTotoalRows { it -1 }
        }

        val resetButton = v.findViewById<ImageButton>(R.id.button_reset)
        resetButton.setOnClickListener {
            ResetRowCounterDialog.create(requireContext()) { updateTotoalRows { 0 } }.show()
        }

        // Inflate the layout for this fragment
        return v
    }

    private fun updateTotoalRows(f: (Int) -> Int) {
        rowCounter = rowCounter.copy(totalRows = f(rowCounter.totalRows))
        textViewRows.text = rowCounter.totalRows.toString()
        KnittingsDataSource.updateRowCounter(rowCounter)
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