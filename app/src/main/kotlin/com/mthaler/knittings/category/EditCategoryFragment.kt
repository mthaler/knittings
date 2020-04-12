package com.mthaler.knittings.category

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.SaveChangesDialog
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category
import petrov.kristiyan.colorpicker.ColorPicker

class EditCategoryFragment : Fragment() {

    private var categoryID: Long = -1
    private lateinit var editTextTitle: EditText
    private lateinit var buttonColor: Button
    private var color: Int? = 0

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

        val category = datasource.getCategory(categoryID)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_edit_category, container, false)

        // set edit text title text to category name
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

        buttonColor.setOnClickListener { view ->
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
                val oldCategory = if (categoryID != -1L) datasource.getCategory(categoryID) else null
                val newCategory = Category(categoryID, editTextTitle.text.toString(), color)
                if (newCategory != oldCategory) {
                    saveCategory(newCategory)
                }
                fragmentManager?.popBackStack()
                true
            }
            R.id.menu_item_delete_category -> {
                context?.let {
                    DeleteDialog.create(it, editTextTitle.text.toString(), {
                        deleteCategory()
                        fragmentManager?.popBackStack()
                    }).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed() {
        context?.let {
            val oldCategory = if (categoryID != -1L) datasource.getCategory(categoryID) else null
            val newCategory = Category(categoryID, editTextTitle.text.toString(), color)
            if (newCategory != oldCategory) {
                SaveChangesDialog.create(it, {
                    saveCategory(newCategory)
                    fragmentManager?.popBackStack()
                }, {
                    fragmentManager?.popBackStack()
                }).show()
            } else {
                fragmentManager?.popBackStack()
            }
        }
    }

    private fun deleteCategory() {
        val category = datasource.getCategory(categoryID)
        datasource.deleteCategory(category)
    }

    private fun saveCategory(category: Category) {
        if (category.id == -1L) {
            datasource.addCategory(category)
        } else {
            datasource.updateCategory(category)
        }
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
