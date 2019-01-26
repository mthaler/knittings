package com.mthaler.knittings.category

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mthaler.knittings.Extras
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_edit_category.*

class EditCategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the category that should be displayed.
        val categoryID = intent.getLongExtra(Extras.EXTRA_CATEGORY_ID, -1L)

        if (savedInstanceState == null) {
            val f = EditCategoryFragment.newInstance(categoryID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.edit_category_container, f)
            //ft.addToBackStack(null)
            ft.commit()
        }
    }

    /**
     * Called when the activity has detected the user's press of the back key. The default implementation simply
     * finishes the current activity, but you can override this to do whatever you want.
     */
    override fun onBackPressed() {
        val f = supportFragmentManager.findFragmentById(R.id.edit_category_container) as HasCategory
        f?.let {
            val category = it.getCategory()
            category?.let {
                val i = Intent()
                i.putExtra(EXTRA_CATEGORY_ID, it.id)
                setResult(RESULT_OK, i)
            }
        }
        super.onBackPressed()
    }
}
