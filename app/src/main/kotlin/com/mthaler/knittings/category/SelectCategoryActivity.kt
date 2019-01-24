package com.mthaler.knittings.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import kotlinx.android.synthetic.main.activity_select_category.*
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import org.jetbrains.anko.startActivity

class SelectCategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // add new category if the user clicks the floating action button and start the
        // EditCategoryActivity to edit the new category
        val fab = findViewById<FloatingActionButton>(R.id.fab_create_category)
        fab.setOnClickListener { v -> run {
            val category = datasource.createCategory("category", null)
            startActivity<EditCategoryActivity>(EXTRA_CATEGORY_ID to category.id)
        } }

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
        val adapter = CategoryAdapter(this, categories, OnItemClickListener { item -> })
        rv.adapter = adapter
    }
}
