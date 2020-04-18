package com.mthaler.knittings.needle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import com.mthaler.knittings.*
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import com.mthaler.knittings.Extras.EXTRA_NEEDLE_ID

class EditNeedleFragment : Fragment() {

    private var needleID: Long = -1
    private lateinit var editTextName: EditText
    private lateinit var editTextSize: EditText
    private lateinit var editTextLength: EditText
    private lateinit var spinnerMaterial: Spinner
    private lateinit var checkBoxInUse: CheckBox
    private lateinit var spinnerType: Spinner

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        val v = inflater.inflate(R.layout.fragment_edit_needle, container, false)

        editTextName = v.findViewById(R.id.needle_name)
        editTextSize = v.findViewById(R.id.needle_size)
        editTextLength = v.findViewById(R.id.needle_length)
        spinnerMaterial = v.findViewById(R.id.needle_material)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(context, android.R.layout.simple_spinner_item, NeedleMaterial.formattedValues(v.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerMaterial.adapter = adapter
        }
        checkBoxInUse = v.findViewById(R.id.needle_in_use)
        spinnerType = v.findViewById(R.id.needle_type)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(context, android.R.layout.simple_spinner_item, NeedleType.formattedValues(v.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerType.adapter = adapter
        }

        if (savedInstanceState == null) {
            val needle = datasource.getNeedle(needleID)
            editTextName.setText(needle.name)
            editTextSize.setText(needle.size)
            editTextLength.setText(needle.length)
            NeedleMaterial.values().indexOf(needle.material).also { index ->
                if (index >= 0) {
                    spinnerMaterial.setSelection(index)
                } else {
                    spinnerMaterial.setSelection(0)
                }
            }
            checkBoxInUse.isChecked = needle.inUse
            NeedleType.values().indexOf(needle.type).also { index ->
                if (index >= 0) {
                    spinnerType.setSelection(index)
                } else {
                    spinnerType.setSelection(0)
                }
            }
        }

        return v
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
                val oldNeedle = if (needleID != -1L) datasource.getNeedle(needleID) else null
                val newNeedle = createNeedle()
                if (newNeedle != oldNeedle) {
                    saveNeedle(newNeedle)
                }
                parentFragmentManager.popBackStack()
                true
            }
            R.id.menu_item_delete_needle -> {
                context?.let {
                    DeleteDialog.create(it, editTextName.text.toString(), {
                        deleteNeedle()
                        parentFragmentManager.popBackStack() }
                    ).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed() {
        context?.let {
            val oldNeedle = if (needleID != -1L) datasource.getNeedle(needleID) else null
            val newNeedle = createNeedle()
            if (newNeedle != oldNeedle) {
                SaveChangesDialog.create(it, {
                    saveNeedle(newNeedle)
                    parentFragmentManager.popBackStack()
                }, {
                    parentFragmentManager.popBackStack()
                }).show()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun createNeedle(): Needle {
        val material = NeedleMaterial.values()[spinnerMaterial.selectedItemPosition]
        val type = NeedleType.values()[spinnerType.selectedItemPosition]
        return Needle(needleID, editTextName.text.toString(), "", editTextSize.text.toString(), editTextLength.text.toString(), material, checkBoxInUse.isChecked, type)
    }

    private fun deleteNeedle() {
        val needle = datasource.getNeedle(needleID)
        datasource.deleteNeedle(needle)
    }

    private fun saveNeedle(needle: Needle) {
        if (needle.id == -1L) {
            datasource.addNeedle(needle)
        } else {
            datasource.updateNeedle(needle)
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
