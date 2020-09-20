package com.mthaler.knittings.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.dbapp.model.Category
import kotlinx.android.synthetic.main.activity_category_list.*

class CategoryListActivity : BaseActivity(), CategoryListFragment.OnFragmentInteractionListener, EditCategoryFragment.OnFragmentInteractionListener {

    private var emptyListBackground: Int = -1
    private var listBackground: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emptyListBackground = if (savedInstanceState != null) savedInstanceState.getInt(EXTRA_EMPTY_LIST_BACKGROUND) else intent.getIntExtra(EXTRA_EMPTY_LIST_BACKGROUND, -1)
        listBackground = if (savedInstanceState != null) savedInstanceState.getInt(EXTRA_LIST_BACKGROUND) else intent.getIntExtra(EXTRA_LIST_BACKGROUND, -1)

        if (savedInstanceState == null) {
            val f = CategoryListFragment.newInstance(-1, emptyListBackground)
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.category_list_container, f)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(EXTRA_EMPTY_LIST_BACKGROUND, emptyListBackground)
        savedInstanceState.putInt(EXTRA_LIST_BACKGROUND, listBackground)
        super.onSaveInstanceState(savedInstanceState)
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
                    f.onBackPressed { fm.popBackStack() }
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
            f.onBackPressed { fm.popBackStack() }
        } else {
            super.onBackPressed()
        }
    }

    override fun createCategory() {
        val f = EditCategoryFragment.newInstance(Category.EMPTY.id)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.category_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun categoryClicked(categoryID: Long) {
        val f = EditCategoryFragment.newInstance(categoryID)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.category_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun categorySaved(categoryID: Long) {
        supportFragmentManager.popBackStack()
    }

    companion object {

        val EXTRA_EMPTY_LIST_BACKGROUND = "com.mthaler.dbapp.empty_list_background"
        val EXTRA_LIST_BACKGROUND = "com.mthaler.dbapp.list_background"

        fun newIntent(context: Context, emptyListBackground: Int, listBackground: Int): Intent {
            val intent = Intent(context, CategoryListActivity::class.java)
            intent.putExtra(EXTRA_EMPTY_LIST_BACKGROUND, emptyListBackground)
            intent.putExtra(EXTRA_EMPTY_LIST_BACKGROUND, listBackground)
            return intent
        }
    }
}
