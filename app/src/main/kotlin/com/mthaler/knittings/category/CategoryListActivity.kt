package com.mthaler.knittings.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.startActivity
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import kotlinx.android.synthetic.main.activity_category_list.*

/**
 * CategoryListActivity displays a list of categories. The user can add a new category by clicking
 * the floating action button and edit existing categories by clicking the category.
 */
class CategoryListActivity : AppCompatActivity(), CategoryListFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the category that should be displayed.
        val knittingID = intent.getLongExtra(EXTRA_KNITTING_ID, -1L)

        if (savedInstanceState == null) {
            val f = CategoryListFragment.newInstance(knittingID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.category_list_container, f)
            //ft.addToBackStack(null)
            ft.commit()
        }
    }

    override fun createCategory() {
        val category = datasource.createCategory("category", null)
        startActivity<EditCategoryActivity>(EXTRA_CATEGORY_ID to category.id)
    }

    override fun categoryClicked(categoryID: Long) {
        startActivity<EditCategoryActivity>(EXTRA_CATEGORY_ID to categoryID)
    }
}
