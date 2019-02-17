package com.mthaler.knittings.needle

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.*
import android.widget.*
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import org.jetbrains.anko.support.v4.alert
import com.mthaler.knittings.model.NeedleType

class EditNeedleFragment : Fragment() {

    private lateinit var needle: Needle

    /**
     * Called to do initial creation of a fragment. This is called after onAttach(Activity) and before
     * onCreateView(LayoutInflater, ViewGroup, Bundle). Note that this can be called while the fragment's activity
     * is still in the process of being created. As such, you can not rely on things like the activity's content view
     * hierarchy being initialized at this point. If you want to do work once the activity itself is created,
     * see onActivityCreated(Bundle).
     *
     * Any restored child fragments will be created before the base Fragment.onCreate method returns.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val needleID = it.getLong(Extras.EXTRA_NEEDLE_ID)
            needle = datasource.getNeedle(needleID)
        }

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional, and non-graphical
     * fragments can return null (which is the default implementation). This will be called between onCreate(Bundle)
     * and onActivityCreated(Bundle).
     *
     * If you return a View from here, you will later be called in onDestroyView() when the view is being released.
     *
     * @param inflater the LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_edit_needle, container, false)

        // set edit text title text to category name
        val editTextName = v.findViewById<EditText>(R.id.needle_name)
        editTextName.addTextChangedListener(createTextWatcher { c, needle -> needle.copy(name = c.toString()) })
        editTextName.setText(needle.name)

        // set edit text title text to category name
        val editTextSize = v.findViewById<EditText>(R.id.needle_size)
        editTextSize.addTextChangedListener(createTextWatcher { c, needle -> needle.copy(size = c.toString()) })
        editTextSize.setText(needle.size)

        // set edit text title text to category name
        val editTextLength = v.findViewById<EditText>(R.id.needle_length)
        editTextLength.addTextChangedListener(createTextWatcher { c, needle -> needle.copy(length = c.toString()) })
        editTextLength.setText(needle.length)

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

    /**
     * Initialize the contents of the Fragment host's standard options menu. You should place your menu items in to menu.
     * For this method to be called, you must have first called setHasOptionsMenu(boolean).
     * See Activity.onCreateOptionsMenu for more information.
     *
     * @param menu The options menu in which you place your items.
     * @param inflater MenuInflater
     */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menu != null && inflater != null) {
            inflater.inflate(R.menu.edit_needle, menu)
        }
    }

    /**
     * Creates a text watcher that updates the category using the given update function
     *
     * @param updateNeedle function to updated the needle
     */
    private fun createTextWatcher(updateNeedle: (CharSequence, Needle) -> Needle): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(c: Editable) {
                val n = updateNeedle(c, needle)
                datasource.updateNeedle(n)
                needle = n
            }
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            return when (item.itemId) {
                R.id.menu_item_delete_needle -> {
                    showDeleteDialog()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Displays a dialog that asks the user to confirm that the knitting should be deleted
     */
    private fun showDeleteDialog() {
        // show alert asking user to confirm that knitting should be deleted
        alert {
            title = resources.getString(R.string.delete_needle_dialog_title)
            message = resources.getString(R.string.delete_needle_dialog_question)
            positiveButton(resources.getString(R.string.delete_needle_dialog_delete_button)) {
                // delete database entry
                datasource.deleteNeedle(needle)
                // go back to the previous fragment which is the category list
                fragmentManager?.popBackStack()
            }
            negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
        }.show()
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
