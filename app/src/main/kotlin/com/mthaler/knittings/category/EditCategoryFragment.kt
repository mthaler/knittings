package com.mthaler.knittings.category

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.*
import android.widget.Button
import android.widget.EditText
import com.android.colorpicker.ColorPickerDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Category
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.support.v4.alert

class EditCategoryFragment : Fragment(), HasCategory {

    private var category: Category? = null

    override fun getCategory(): Category? {
        return category
    }

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
            val categoryID = it.getLong(EXTRA_CATEGORY_ID)
            category = datasource.getCategory(categoryID)
        }
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
        val v = inflater.inflate(R.layout.fragment_edit_category, container, false)

        // set edit text title text to category name
        val editTextTitle = v.findViewById<EditText>(R.id.category_name)
        editTextTitle.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(name = c.toString()) })
        category?.let { editTextTitle.setText(it.name) }

        // set background color of the button to category color if it is defined
        val button = v.findViewById<Button>(R.id.button_select_color)
        category?.let { if (it.color != null) button.setBackgroundColor(it.color) }
        button.setOnClickListener { view -> run {
            val colorPickerDialog = ColorPickerDialog()
            colorPickerDialog.initialize(R.string.category_color_dialog_title, COLORS, Color.RED, 4, COLORS.size)
            colorPickerDialog.setOnColorSelectedListener { color -> run {
                val category0 = category
                if (category0 != null) {
                    try {
                        val category1 = category0.copy(color = color)
                        datasource.updateCategory(category1)
                        category = category1
                        button.setBackgroundColor(color)
                    } catch(ex: Exception) {
                    }

                }
                button.setBackgroundColor(color)
            } }
            colorPickerDialog.show(fragmentManager, null)
        }}

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
            inflater.inflate(R.menu.edit_category, menu)
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
                R.id.menu_item_delete_category -> {
                    showDeleteDialog()
                    true
                }
                android.R.id.home -> {
                    category?.let {
                        val i = Intent()
                        i.putExtra(EXTRA_CATEGORY_ID, it.id)
                        activity?.let {  it.setResult(AppCompatActivity.RESULT_OK, i) }
                    }
                    activity?.let { it.finish() }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed in a
     * new instance of its process is restarted. If a new instance of the fragment later needs to be created,
     * the data you place in the Bundle here will be available in the Bundle given to onCreate(Bundle),
     * onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
     *
     * @param outState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        category?.let { outState.putLong(EXTRA_CATEGORY_ID, it.id) }
        super.onSaveInstanceState(outState)
    }

    /**
     * Displays a dialog that asks the user to confirm that the knitting should be deleted
     */
    private fun showDeleteDialog() {
        // show alert asking user to confirm that knitting should be deleted
        alert {
            title = resources.getString(R.string.delete_category_dialog_title)
            message = resources.getString(R.string.delete_category_dialog_question)
            positiveButton(resources.getString(R.string.delete_category_dialog_delete_button)) {
                // delete database entry
                category?.let { datasource.deleteCategory(it) }
                activity?.let { it.finish() }
            }
            negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
        }.show()
    }


    /**
     * Creates a text watcher that updates the category using the given update function
     *
     * @param updateCategory function to updated the category
     */
    private fun createTextWatcher(updateCategory: (CharSequence, Category) -> Category): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(c: Editable) {
                category?.let {
                    try {
                        val c = updateCategory(c, it)
                        datasource.updateCategory(c)
                        category = c
                    } catch(ex: Exception) {
                    }
                }
            }
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param categoryID id of the category that should be edited
         * @return A new instance of fragment EditCategoryFragment.
         */
        @JvmStatic
        fun newInstance(categoryID: Long) =
            EditCategoryFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_CATEGORY_ID, categoryID)
                }
            }

        val COLORS = arrayOf("#F6402C", "#EB1460", "#9C1AB1", "#6633B9", "#3D4DB7", "#1093F5", "#00A6F6", "#00BBD5", "#009687", "#46AF4A",
                "#88C440", "#CCDD1E", "#FFEC16", "#FFC100", "#FF9800", "#FF5505", "#7A5547", "#9D9D9D", "#5E7C8B").map { Color.parseColor(it) }.toIntArray()
    }
}
