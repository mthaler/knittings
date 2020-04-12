package com.mthaler.knittings.category

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_select_category.*
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category

class SelectCategoryActivity : BaseActivity(), CategoryListFragment.OnFragmentInteractionListener {

    var knittingID = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the category that should be displayed.
        knittingID = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)

        if (savedInstanceState == null) {
            val f = CategoryListFragment.newInstance(knittingID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.select_category_container, f)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        val f = fm.findFragmentById(R.id.select_category_container)
        if (f is EditCategoryFragment) {
            val i = Intent()
            i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategoryID())
            setResult(Activity.RESULT_OK, i)
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            val fm = supportFragmentManager
            val f = fm.findFragmentById(R.id.select_category_container)
            if (f is EditCategoryFragment) {
                val i = Intent()
                i.putExtra(EXTRA_KNITTING_ID, knittingID)
                i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategoryID())
                setResult(Activity.RESULT_OK, i)
                finish()
            } else {
                val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
                if (upIntent == null) {
                    throw IllegalStateException("No Parent Activity Intent")
                } else {
                    upIntent.putExtra(EXTRA_KNITTING_ID, knittingID)
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun createCategory() {
        val category = datasource.addCategory(Category())
        val f = EditCategoryFragment.newInstance(category.id)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.select_category_container, f)
        ft.commit()
    }

    override fun categoryClicked(categoryID: Long) {
        val i = Intent()
        i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}
