package com.mthaler.knittings.needle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.*
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType

class EditNeedleFragment : Fragment() {

    private var needleID: Long = -1
    private lateinit var viewModel: EditNeedleViewModel
    private lateinit var editTextName: EditText
    private lateinit var editTextSize: EditText
    private lateinit var editTextLength: EditText
    private lateinit var spinnerMaterial: Spinner
    private lateinit var checkBoxInUse: CheckBox
    private lateinit var spinnerType: Spinner
    private var moddified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            needleID = it.getLong(Extras.EXTRA_NEEDLE_ID)
        }

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        val v = inflater.inflate(R.layout.fragment_edit_needle, container, false)

        editTextName = v.findViewById(R.id.needle_name)
        editTextName.addTextChangedListener(createTextWatcher())

        editTextSize = v.findViewById(R.id.needle_size)
        editTextSize.addTextChangedListener(createTextWatcher())

        editTextLength = v.findViewById(R.id.needle_length)
        editTextLength.addTextChangedListener(createTextWatcher())

        spinnerMaterial = v.findViewById(R.id.needle_material)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(context, android.R.layout.simple_spinner_item, NeedleMaterial.formattedValues(v.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerMaterial.adapter = adapter
        }
        spinnerMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                moddified = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        checkBoxInUse = v.findViewById(R.id.needle_in_use)
        checkBoxInUse.setOnCheckedChangeListener { view, checked ->
            moddified = true
        }

        spinnerType = v.findViewById(R.id.needle_type)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(context, android.R.layout.simple_spinner_item, NeedleType.formattedValues(v.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerType.adapter = adapter
        }
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                moddified = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)).get(EditNeedleViewModel::class.java)
        viewModel.init(needleID)
        viewModel.needle.observe(viewLifecycleOwner, Observer { needle ->
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
            NeedleType.values().indexOf(needle.type).also {index ->
                if (index >= 0) {
                    spinnerType.setSelection(index)
                } else {
                    spinnerType.setSelection(0)
                }
            }
            moddified = false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_needle, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_save_needle -> {
                if (moddified) {
                    viewModel.saveNeedle(createNeedle())
                }
                fragmentManager?.popBackStack()
                true
            }
            R.id.menu_item_delete_needle -> {
                context?.let {
                    DeleteDialog.create(it, editTextName.text.toString(), {
                        viewModel.deleteNeedle()
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
            if (moddified) {
                SaveChangesDialog.create(it, {
                    viewModel.saveNeedle(createNeedle())
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
                moddified = true
            }
        }
    }

    private fun createNeedle(): Needle {
        val material = NeedleMaterial.values()[spinnerMaterial.selectedItemPosition]
        val type = NeedleType.values()[spinnerType.selectedItemPosition]
        return Needle(needleID, editTextName.text.toString(), "", editTextSize.text.toString(), editTextLength.text.toString(), material, checkBoxInUse.isSelected, type)
    }

    companion object {

        @JvmStatic
        fun newInstance(needleID: Long) =
                EditNeedleFragment().apply {
                    arguments = Bundle().apply {
                        putLong(Extras.EXTRA_NEEDLE_ID, needleID)
                    }
                }
    }
}
