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
import com.android.colorpicker.ColorPickerDialog

class EditCategoryActivity : AppCompatActivity() {

    private var category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category)

        // get the id of the category that should be displayed.
        val id = intent.getLongExtra(EXTRA_CATEGORY_ID, -1L)
        if (id != -1L) {
            category = datasource.getCategory(id)
        }

        val editTextTitle = findViewById<EditText>(R.id.category_name)
        editTextTitle.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(name = c.toString()) })
        editTextTitle.setText(category!!.name)

        val button = findViewById<Button>(R.id.button_select_color)
        if (category!!.color != null) {
            button.setBackgroundColor(category!!.color!!)
        }
        button.setOnClickListener { view -> run {
            val colorPickerDialog = ColorPickerDialog()
            colorPickerDialog.initialize(R.string.delete_photo, COLORS, Color.RED, 4, COLORS.size)
            colorPickerDialog.setOnColorSelectedListener({ color -> run {
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
            } })
            colorPickerDialog.show(getFragmentManager(), null)
        }}
    }

    /**
     * Creates a textwatcher that updates the knitting using the given update function
     *
     * @param updateKnitting function to updated the knitting
     */
    private fun createTextWatcher(updateCategory: (CharSequence, Category) -> Category): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(c: Editable) {
                val category0 = category
                if (category0 != null) {
                    try {
                        val category1 = updateCategory(c, category0)
                        datasource.updateCategory(category1)
                        category = category1
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
