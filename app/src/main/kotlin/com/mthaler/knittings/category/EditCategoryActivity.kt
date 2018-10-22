package com.mthaler.knittings.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import com.mthaler.knittings.R
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category

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
    }
}
