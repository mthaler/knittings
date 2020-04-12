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

class EditNeedleFragment : Fragment() {

    private var needleID: Long = -1
    private lateinit var editTextName: EditText
    private lateinit var editTextSize: EditText
    private lateinit var editTextLength: EditText
    private lateinit var spinnerMaterial: Spinner
    private lateinit var checkBoxInUse: CheckBox
    private lateinit var spinnerType: Spinner
    private var modified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            needleID = it.getLong(Extras.EXTRA_NEEDLE_ID)
        }
        savedInstanceState?.let {
            if (it.containsKey(EXTRA_MODIFIED)) {
                modified = it.getBoolean(EXTRA_MODIFIED)
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

        editTextName.addTextChangedListener(createTextWatcher())
        editTextSize.addTextChangedListener(createTextWatcher())
        editTextLength.addTextChangedListener(createTextWatcher())
        spinnerMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                modified = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        checkBoxInUse.setOnCheckedChangeListener { view, checked ->
            modified = true
        }
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                modified = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return v
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean(EXTRA_MODIFIED, modified)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_needle, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_save_needle -> {
                if (modified) {
                    saveNeedle(createNeedle())
                }
                fragmentManager?.popBackStack()
                true
            }
            R.id.menu_item_delete_needle -> {
                context?.let {
                    DeleteDialog.create(it, editTextName.text.toString(), {
                        deleteNeedle()
                        fragmentManager?.popBackStack() }
                    ).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed() {
        context?.let {
            if (modified) {
                SaveChangesDialog.create(it, {
                    saveNeedle(createNeedle())
                    fragmentManager?.popBackStack()
                }, {
                    fragmentManager?.popBackStack()
                }).show()
            } else {
                fragmentManager?.popBackStack()
            }
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                modified = true
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

        const val EXTRA_MODIFIED = "com.mthaler.knittings.needle.MODIFIED"

        @JvmStatic
        fun newInstance(needleID: Long) =
                EditNeedleFragment().apply {
                    arguments = Bundle().apply {
                        putLong(Extras.EXTRA_NEEDLE_ID, needleID)
                    }
                }
    }
}
