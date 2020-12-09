package com.mthaler.knittings.rowcounter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.Extras
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentRowCounterBinding
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.RowCounter

class RowCounterFragment : Fragment() {

    private var knittingID: Long = Knitting.EMPTY.id
    private var rowCounter: RowCounter = RowCounter.EMPTY
    private var _binding: FragmentRowCounterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentRowCounterBinding.inflate(inflater, container, false)
        val view = binding.root

        val knitting = KnittingsDataSource.getProject(knittingID)
        val rc = KnittingsDataSource.getRowCounter(knitting)
        rowCounter = if (rc != null) rc else KnittingsDataSource.addRowCounter(RowCounter(-1, 0, 1, knittingID))

        binding.rows.text = rowCounter.totalRows.toString()

        binding.buttonPlus.setOnClickListener {
            updateTotoalRows { it + 1 }

        }

        binding.buttonMinus.setOnClickListener {
            updateTotoalRows { r -> if (r > 0) r - 1 else r }
        }

        binding.buttonReset.setOnClickListener {
            ResetRowCounterDialog.create(requireContext()) { updateTotoalRows { 0 } }.show()
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTotoalRows(f: (Int) -> Int) {
        rowCounter = rowCounter.copy(totalRows = f(rowCounter.totalRows))
        binding.rows.text = rowCounter.totalRows.toString()
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