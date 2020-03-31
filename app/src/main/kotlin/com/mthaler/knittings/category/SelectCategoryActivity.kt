package com.mthaler.knittings.category

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_select_category.*
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category

class SelectCategoryActivity : AppCompatActivity(), CategoryListFragment.OnFragmentInteractionListener {

    var knittingID = -1L

    /**
     * Called when the activity is starting. This is where most initialization should go: calling setContentView(int)
     * to inflate the activity's UI, using findViewById(int) to programmatically interact with widgets in the UI,
     * calling managedQuery(android.net.Uri, String[], String, String[], String) to retrieve cursors for data being displayed, etc.
     *
     * @param savedInstanceState Bundle: If the activity is being re-initialized after previously being shut down then this Bundle contains
     *                           the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
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
            //ft.addToBackStack(null)
            ft.commit()
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed in a
     * new instance of its process is restarted. If a new instance of the fragment later needs to be created,
     * the data you place in the Bundle here will be available in the Bundle given to onCreate(Bundle),
     * onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
     *
     * @param outState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(outState)
    }

    /**
     * Called when the activity has detected the user's press of the back key. The default implementation simply
     * finishes the current activity, but you can override this to do whatever you want.
     */
    override fun onBackPressed() {
        val fm = supportFragmentManager
        val f = fm.findFragmentById(R.id.select_category_container)
        if (f is EditCategoryFragment) {
            val i = Intent()
            i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategory())
            setResult(Activity.RESULT_OK, i)
        }
        super.onBackPressed()
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            val fm = supportFragmentManager
            val f = fm.findFragmentById(R.id.select_category_container)
            if (f is EditCategoryFragment) {
                val i = Intent()
                i.putExtra(EXTRA_KNITTING_ID, knittingID)
                i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategory().id)
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
        //ft.addToBackStack(null)
        ft.commit()
    }

    override fun categoryClicked(categoryID: Long) {
        val i = Intent()
        i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}
