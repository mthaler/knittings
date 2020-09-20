package com.mthaler.knittings.category

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_select_category.*
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.dbapp.model.Category

class SelectCategoryActivity : BaseActivity(), CategoryListFragment.OnFragmentInteractionListener, EditCategoryFragment.OnFragmentInteractionListener {

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
            val f = CategoryListFragment.newInstance(-1, -1)
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
        val f =  supportFragmentManager.findFragmentById(R.id.select_category_container)
        if (f is EditCategoryFragment) {
            f.onBackPressed { categoryID ->
                val i = Intent()
                i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategoryID())
                setResult(Activity.RESULT_OK, i)
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            val f =  supportFragmentManager.findFragmentById(R.id.select_category_container)
            if (f is EditCategoryFragment) {
                f.onBackPressed { categoryID ->
                    if (f.getCategoryID() == Category.EMPTY.id) {
                        finish()
                    } else {
                        val i = Intent()
                        i.putExtra(EXTRA_KNITTING_ID, knittingID)
                        i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategoryID())
                        setResult(Activity.RESULT_OK, i)
                        finish()
                    }
                }
            } else {
                finish()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun createCategory() {
        val f = EditCategoryFragment.newInstance(Category.EMPTY.id)
        val ft =  supportFragmentManager.beginTransaction()
        ft.replace(R.id.select_category_container, f)
        ft.commit()
    }

    override fun categoryClicked(categoryID: Long) {
        val i = Intent()
        i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    override fun categorySaved(categoryID: Long) {
        if (categoryID == Category.EMPTY.id) {
            finish()
        } else {
            val i = Intent()
            i.putExtra(EXTRA_KNITTING_ID, knittingID)
            i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }
}
