package com.mthaler.knittings.category

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_select_category.*
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.database.datasource

class SelectCategoryActivity : AppCompatActivity(), CategoryListFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the category that should be displayed.
        val knittingID = intent.getLongExtra(EXTRA_KNITTING_ID, -1L)

        if (savedInstanceState == null) {
            val f = CategoryListFragment.newInstance(knittingID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.select_category_container, f)
            //ft.addToBackStack(null)
            ft.commit()
        }
    }

    /**
     * Called when the activity has detected the user's press of the back key. The default implementation simply
     * finishes the current activity, but you can override this to do whatever you want.
     */
    override fun onBackPressed() {
        val fm = supportFragmentManager
        val f = fm.findFragmentById(R.id.select_category_container) as HasCategory
        f?.let {
            val c = it.getCategory()
            c?.let {
                val i = Intent()
                i.putExtra(Extras.EXTRA_CATEGORY_ID, it.id)
                setResult(Activity.RESULT_OK, i)
            }
        }
        super.onBackPressed()
    }

    override fun createCategory() {
        val category = datasource.createCategory("", null)
        val f = EditCategoryFragment.newInstance(category.id)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.select_category_container, f)
        //ft.addToBackStack(null)
        ft.commit()
    }

    override fun categoryClicked(categoryID: Long) {
        val i = Intent()
        i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
//        android.R.id.home -> {
//            // Respond to the action bar's Up/Home button
//            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
//            if (upIntent == null) {
//                throw IllegalStateException("No Parent Activity Intent")
//            } else {
//                upIntent.putExtra(EXTRA_KNITTING_ID, knittingID)
//                NavUtils.navigateUpTo(this, upIntent)
//            }
//            true
//        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_EDIT_CATEGORY = 0
    }
}
