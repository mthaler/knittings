package com.mthaler.knittings.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.startActivity

/**
 * CategoryListActivity displays a list of categories. The user can add a new category by clicking
 * the floating action button and edit existing categories by clicking the category.
 */
class CategoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        // add new category if the user clicks the floating action button and start the
        // EditCategoryActivity to edit the new category
        val fab = findViewById<FloatingActionButton>(R.id.fab_create_category)
        fab.setOnClickListener { v -> run {
            val category = datasource.createCategory("category", null)
            startActivity<EditCategoryActivity>(EditCategoryActivity.EXTRA_CATEGORY_ID to category.id)
        } }

        val rv = findViewById<RecyclerView>(R.id.recyclerView)
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
        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        val categories = datasource.allCategories
        // start EditCategoryActivity if the users clicks on a category
        val adapter = CategoryAdapter(this, categories, OnItemClickListener { item -> startActivity<EditCategoryActivity>(EditCategoryActivity.EXTRA_CATEGORY_ID to item!!.id) })
        rv.adapter = adapter
    }
}
