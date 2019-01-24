package com.mthaler.knittings.category

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import kotlinx.android.synthetic.main.activity_select_category.*
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.model.Category
import org.jetbrains.anko.startActivity

class SelectCategoryActivity : AppCompatActivity() {

    private var knittingID = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the knitting for the stopwatch
        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            knittingID = id
        } else {
            error("Could not get knitting id")
        }

        // add new category if the user clicks the floating action button and start the
        // EditCategoryActivity to edit the new category
        val fab = findViewById<FloatingActionButton>(R.id.fab_create_category)
        fab.setOnClickListener { v ->
            val category = datasource.createCategory("category", null)
            startActivity<EditCategoryActivity>(EXTRA_CATEGORY_ID to category.id)
        }

        val rv = findViewById<RecyclerView>(R.id.category_recycler_view)
        rv.layoutManager = LinearLayoutManager(this)
    }

    /**
     * The onResume method is called when the activity is started or if the user returns from another activity
     * e.g. the EditCategoryActivity.
     */
    override fun onResume() {
        super.onResume()
        updateCategoryList()
    }

    /**
     * This method is called if the activity gets destroyed because e.g. the device configuration changes because the device is rotated
     * We need to store instance variables because they are not automatically restored
     *
     * @param savedInstanceState saved instance state
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                upIntent.putExtra(EXTRA_KNITTING_ID, knittingID)
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Updates the list of categories
     */
    private fun updateCategoryList() {
        val rv = findViewById<RecyclerView>(R.id.category_recycler_view)
        val categories = datasource.allCategories
        // show image if category list is empty
        if (categories.isEmpty()) {
            category_empty_recycler_view.visibility = View.VISIBLE
            category_recycler_view.visibility = View.GONE
        } else {
            category_empty_recycler_view.visibility = View.GONE
            category_recycler_view.visibility = View.VISIBLE
        }
        // start EditCategoryActivity if the users clicks on a category
        val adapter = CategoryAdapter(this, categories, object : OnItemClickListener {
            override fun onItemClick(item: Category) {
                val i = Intent()
                i.putExtra(EXTRA_CATEGORY_ID, item.id)
                setResult(Activity.RESULT_OK, i)
                finish()
            }
        })
        rv.adapter = adapter
    }
}
