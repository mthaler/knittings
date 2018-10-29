package com.mthaler.knittings.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import com.mthaler.knittings.R
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import com.android.colorpicker.ColorPickerDialog
import kotlinx.android.synthetic.main.activity_edit_category.*
import org.jetbrains.anko.alert

class EditCategoryActivity : AppCompatActivity() {

    private var category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category)

        setSupportActionBar(toolbar)

        // get the id of the category that should be displayed.
        val id = intent.getLongExtra(EXTRA_CATEGORY_ID, -1L)
        if (id != -1L) {
            category = datasource.getCategory(id)
        }

        // set edit text title text to category name
        val editTextTitle = findViewById<EditText>(R.id.category_name)
        editTextTitle.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(name = c.toString()) })
        category?.let { editTextTitle.setText(it.name) }

        // set background color of the button to category color if it is defined
        val button = findViewById<Button>(R.id.button_select_color)
        category?.let { if (it.color != null) button.setBackgroundColor(it.color) }
        button.setOnClickListener { view -> run {
            val colorPickerDialog = ColorPickerDialog()
            colorPickerDialog.initialize(R.string.delete_photo, COLORS, Color.RED, 4, COLORS.size)
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.edit_category, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_delete_category -> {
            showDeleteDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Displays a dialog that asks the user to confirm that the knitting should be deleted
     */
    private fun showDeleteDialog() {
        // show alert asking user to confirm that knitting should be deleted
        alert {
            title = resources.getString(R.string.delete_knitting_dialog_title)
            message = resources.getString(R.string.delete_knitting_dialog_question)
            positiveButton(resources.getString(R.string.delete_knitting_dialog_delete_button)) {
                // delete database entry
                datasource.deleteCategory(category!!)
                finish()
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
        const val EXTRA_CATEGORY_ID = "com.mthaler.knitting.CATEGORY_ID"

        val COLORS = arrayOf("#F6402C", "#EB1460", "#9C1AB1", "#6633B9", "#3D4DB7", "#1093F5", "#00A6F6", "#00BBD5", "#009687", "#46AF4A",
                "#88C440", "#CCDD1E", "#FFEC16", "#FFC100", "#FF9800", "#FF5505", "#7A5547", "#9D9D9D", "#5E7C8B").map { Color.parseColor(it) }.toIntArray()
    }
}
