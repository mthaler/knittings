package com.mthaler.knittings.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category
import kotlinx.android.synthetic.main.activity_category_list.*

class CategoryListActivity : BaseActivity(), CategoryListFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val f = CategoryListFragment.newInstance(-1)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.category_list_container, f)
            ft.commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val fm = supportFragmentManager
                val f = fm.findFragmentById(R.id.category_list_container)
                if (f is EditCategoryFragment) {
                    f.onBackPressed()
                } else {
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        val f = fm.findFragmentById(R.id.category_list_container)
        if (f is EditCategoryFragment) {
            f.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun createCategory() {
        val category = datasource.addCategory(Category())
        val f = EditCategoryFragment.newInstance(category.id)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.category_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun categoryClicked(categoryID: Long) {
        val f = EditCategoryFragment.newInstance(categoryID)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.category_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {

        fun newIntent(context: Context): Intent = Intent(context, CategoryListActivity::class.java)
    }
}
