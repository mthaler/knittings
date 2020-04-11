package com.mthaler.knittings.needle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.view.*
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType

class EditNeedleFragment : Fragment() {

    private var needleID: Long = -1
    private lateinit var viewModel: EditNeedleViewModel
    private lateinit var editTextName: EditText
    private lateinit var editTextSize: EditText
    private lateinit var editTextLength: EditText
    private lateinit var needle: Needle
    private var moddified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val needleID = it.getLong(Extras.EXTRA_NEEDLE_ID)
            needle = datasource.getNeedle(needleID)
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

        val spinnerMaterial = v.findViewById<Spinner>(R.id.needle_material)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(context, android.R.layout.simple_spinner_item, NeedleMaterial.formattedValues(v.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerMaterial.adapter = adapter
        }
        NeedleMaterial.values().indexOf(needle.material).also { index ->
            if (index >= 0) {
                spinnerMaterial.setSelection(index)
            } else {
                spinnerMaterial.setSelection(0)
            }
        }

        spinnerMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            /**
             * Callback method to be invoked when an item in this view has been selected. This callback is invoked only when the
             * newly selected position is different from the previously selected position or if there was no selected item.
             *
             * Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
             *
             * @param parent the AdapterView where the selection happened
             * @param view the view within the AdapterView that was clicked
             * @param position the position of the view in the adapter
             * @param id the row id of the item that is selected
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val n = needle.copy(material = NeedleMaterial.values()[position])
                datasource.updateNeedle(n)
                needle = n
            }

            /**
             * Callback method to be invoked when the selection disappears from this view. The selection can disappear
             * for instance when touch is activated or when the adapter becomes empty.
             *
             * @param parent the AdapterView where the selection happened
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val checkBoxinUse = v.findViewById<CheckBox>(R.id.needle_in_use)
        checkBoxinUse.setOnCheckedChangeListener { view, checked ->
            val n = needle.copy(inUse = checked)
            datasource.updateNeedle(n)
            needle = n
        }
        checkBoxinUse.isChecked = needle.inUse

        val spinnerType = v.findViewById<Spinner>(R.id.needle_type)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(context, android.R.layout.simple_spinner_item, NeedleType.formattedValues(v.context)).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerType.adapter = adapter
        }
        NeedleType.values().indexOf(needle.type).also {index ->
            if (index >= 0) {
                spinnerType.setSelection(index)
            } else {
                spinnerType.setSelection(0)
            }
        }

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            /**
             * Callback method to be invoked when an item in this view has been selected. This callback is invoked only when the
             * newly selected position is different from the previously selected position or if there was no selected item.
             *
             * Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
             *
             * @param parent the AdapterView where the selection happened
             * @param view the view within the AdapterView that was clicked
             * @param position the position of the view in the adapter
             * @param id the row id of the item that is selected
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val n = needle.copy(type = NeedleType.values()[position])
                datasource.updateNeedle(n)
                needle = n
            }

            /**
             * Callback method to be invoked when the selection disappears from this view. The selection can disappear
             * for instance when touch is activated or when the adapter becomes empty.
             *
             * @param parent the AdapterView where the selection happened
             */
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
            moddified = false
        })
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu. You should place your menu items in to menu.
     * For this method to be called, you must have first called setHasOptionsMenu(boolean).
     * See Activity.onCreateOptionsMenu for more information.
     *
     * @param menu The options menu in which you place your items.
     * @param inflater MenuInflater
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_needle, menu)
    }

    /**
     * Creates a text watcher that updates the category using the given update function
     *
     * @param updateNeedle function to updated the needle
     */
    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                moddified = true
            }
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_delete_needle -> {
                showDeleteDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Displays a dialog that asks the user to confirm that the knitting should be deleted
     */
    private fun showDeleteDialog() {
        context?.let {
            DeleteNeedleDialog.create(it, needle, {
                // delete database entry
                datasource.deleteNeedle(needle)
                // go back to the previous fragment which is the category list
                fragmentManager?.popBackStack() }
            ).show()
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param needleID id of the needle that should be edited
         * @return A new instance of fragment EditNeedleFragment.
         */
        @JvmStatic
        fun newInstance(needleID: Long) =
                EditNeedleFragment().apply {
                    arguments = Bundle().apply {
                        putLong(Extras.EXTRA_NEEDLE_ID, needleID)
                    }
                }
    }
}
