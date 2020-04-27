package com.mthaler.knittings.category

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.DiscardChangesDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.SaveChangesDialog
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category
import petrov.kristiyan.colorpicker.ColorPicker

class EditCategoryFragment : Fragment() {

    private var categoryID: Long = Category.EMPTY.id
    private lateinit var editTextTitle: EditText
    private lateinit var buttonColor: Button
    private var color: Int? = null
    private var listener: OnFragmentInteractionListener? = null

    fun getCategoryID(): Long = categoryID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryID = it.getLong(EXTRA_CATEGORY_ID)
        }
        savedInstanceState?.let {
            if (it.containsKey(EXTRA_CATEGORY_ID)) {
                categoryID = it.getLong(EXTRA_CATEGORY_ID)
            }
            if (it.containsKey(EXTRA_COLOR)) {
                color = it.getInt(EXTRA_COLOR)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        val category = if (categoryID != Category.EMPTY.id) datasource.getCategory(categoryID) else Category()

        val v = inflater.inflate(R.layout.fragment_edit_category, container, false)

        editTextTitle = v.findViewById(R.id.category_name)
        buttonColor = v.findViewById(R.id.button_select_color)

        if (savedInstanceState == null) {
            editTextTitle.setText(category.name)
            if (category.color != null) {
                buttonColor.setBackgroundColor(category.color)
                color = category.color
            }
        } else {
            color?.let { buttonColor.setBackgroundColor(it) }
        }

        buttonColor.setOnClickListener { view -> showColorPicker() }

        return v
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_CATEGORY_ID, categoryID)
        color?.let { savedInstanceState.putInt(EXTRA_COLOR, it) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_category, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_save_category -> {
                val oldCategory = if (categoryID != Category.EMPTY.id) datasource.getCategory(categoryID) else Category.EMPTY
                val newCategory = Category(categoryID, editTextTitle.text.toString(), color)
                if (newCategory != oldCategory) {
                    saveCategory(newCategory)
                }
                listener?.categorySaved(categoryID)
                true
            }
            R.id.menu_item_delete_category -> {
                if (categoryID == Category.EMPTY.id) {
                    DiscardChangesDialog.create(requireContext(), {
                        parentFragmentManager.popBackStack()
                    }).show()
                } else {
                    DeleteDialog.create(requireContext(), editTextTitle.text.toString(), {
                        deleteCategory()
                        parentFragmentManager.popBackStack()
                    }).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed(action: (Long) -> Unit) {
        val oldCategory = if (categoryID != Category.EMPTY.id) datasource.getCategory(categoryID) else Category.EMPTY
        val newCategory = Category(categoryID, editTextTitle.text.toString(), color)
        if (newCategory != oldCategory) {
            SaveChangesDialog.create(requireContext(), {
                saveCategory(newCategory)
                action(categoryID)
            }, {
                action(categoryID)
            }).show()
        } else {
            action(categoryID)
        }
    }

    private fun showColorPicker() {
        val colorPicker = ColorPicker(activity)
        colorPicker.setOnFastChooseColorListener(object : ColorPicker.OnFastChooseColorListener {
            override fun setOnFastChooseColorListener(position: Int, c: Int) {
                color = c
                buttonColor.setBackgroundColor(c)
            }

            override fun onCancel() {
            }
        })
        colorPicker.setTitle(resources.getString(R.string.category_color_dialog_title))
        colorPicker.setColors(R.array.category_colors)
        colorPicker.setRoundColorButton(true)
        colorPicker.setColorButtonMargin(6, 6, 6, 6)
        colorPicker.setColumns(4)
        colorPicker.show()
    }

    private fun deleteCategory() {
        val category = datasource.getCategory(categoryID)
        datasource.deleteCategory(category)
    }

    private fun saveCategory(category: Category) {
        if (category.id == Category.EMPTY.id) {
            val result = datasource.addCategory(category)
            categoryID = result.id
        } else {
            datasource.updateCategory(category)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {

        fun categorySaved(categoryID: Long)
    }

    companion object {

        private const val EXTRA_COLOR = "com.mthaler.knittings.category.COLOR"

        @JvmStatic
        fun newInstance(categoryID: Long) =
                EditCategoryFragment().apply {
                    arguments = Bundle().apply {
                        putLong(EXTRA_CATEGORY_ID, categoryID)
                    }
                }
    }
}
