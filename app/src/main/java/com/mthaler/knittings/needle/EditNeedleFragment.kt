package com.mthaler.knittings.needle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.SaveChangesDialog
import com.mthaler.knittings.*
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import com.mthaler.knittings.Extras.EXTRA_NEEDLE_ID
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentEditNeedleBinding

class EditNeedleFragment : Fragment() {

    private var needleID: Long = Needle.EMPTY.id
    private var _binding: FragmentEditNeedleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            needleID = it.getLong(EXTRA_NEEDLE_ID)
        }
        savedInstanceState?.let {
            if (it.containsKey(EXTRA_NEEDLE_ID)) {
                needleID = it.getLong(EXTRA_NEEDLE_ID)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditNeedleBinding.inflate(inflater, container, false)
        val view = binding.root

        setHasOptionsMenu(true)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, NeedleMaterial.formattedValues(view.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.needleMaterial.adapter = adapter
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, NeedleType.formattedValues(view.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.needleType.adapter = adapter
        }

        if (savedInstanceState == null) {
            val needle = if (needleID != Needle.EMPTY.id) KnittingsDataSource.getNeedle(needleID) else Needle.EMPTY
            binding.needleName.setText(needle.name)
            binding.needleSize.setText(needle.size)
            binding.needleLength.setText(needle.length)
            NeedleMaterial.values().indexOf(needle.material).also { index ->
                if (index >= 0) {
                    binding.needleMaterial.setSelection(index)
                } else {
                    binding.needleMaterial.setSelection(0)
                }
            }
            binding.needleInUse.isChecked = needle.inUse
            NeedleType.values().indexOf(needle.type).also { index ->
                if (index >= 0) {
                    binding.needleType.setSelection(index)
                } else {
                    binding.needleType.setSelection(0)
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_NEEDLE_ID, needleID)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_needle, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_save_needle -> {
                val oldNeedle = if (needleID != Needle.EMPTY.id) KnittingsDataSource.getNeedle(needleID) else Needle.EMPTY
                val newNeedle = createNeedle()
                if (newNeedle != oldNeedle) {
                    saveNeedle(newNeedle)
                }
                parentFragmentManager.popBackStack()
                true
            }
            R.id.menu_item_delete_needle -> {
                if (needleID == Needle.EMPTY.id) {
                    DiscardChangesDialog.create(requireContext()) {
                        parentFragmentManager.popBackStack()
                    }.show()
                } else {
                    DeleteDialog.create(requireContext(), binding.needleName.text.toString()
                    ) {
                        deleteNeedle()
                        parentFragmentManager.popBackStack()
                    }.show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed() {
        val oldNeedle = if (needleID != Needle.EMPTY.id) KnittingsDataSource.getNeedle(needleID) else Needle.EMPTY
        val newNeedle = createNeedle()
        if (newNeedle != oldNeedle) {
            SaveChangesDialog.create(requireContext(), {
                saveNeedle(newNeedle)
                parentFragmentManager.popBackStack()
            }, {
                parentFragmentManager.popBackStack()
            }).show()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    private fun createNeedle(): Needle {
        val material = NeedleMaterial.values()[binding.needleMaterial.selectedItemPosition]
        val type = NeedleType.values()[binding.needleType.selectedItemPosition]
        return Needle(needleID, binding.needleName.text.toString(), "", binding.needleSize.text.toString(), binding.needleLength.text.toString(), material, binding.needleInUse.isChecked, type)
    }

    private fun deleteNeedle() {
        val needle = KnittingsDataSource.getNeedle(needleID)
        KnittingsDataSource.deleteNeedle(needle)
    }

    private fun saveNeedle(needle: Needle) {
        if (needle.id == Needle.EMPTY.id) {
            KnittingsDataSource.addNeedle(needle)
        } else {
            KnittingsDataSource.updateNeedle(needle)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(needleID: Long) =
                EditNeedleFragment().apply {
                    arguments = Bundle().apply {
                        putLong(EXTRA_NEEDLE_ID, needleID)
                    }
                }
    }
}
