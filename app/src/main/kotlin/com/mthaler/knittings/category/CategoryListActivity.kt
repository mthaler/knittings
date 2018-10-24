package com.mthaler.knittings.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Category
import org.jetbrains.anko.startActivity

class CategoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        val fab = findViewById<FloatingActionButton>(R.id.fab_create_category)
        fab.setOnClickListener({v -> run {
            val category = datasource.createCategory("category", null)
            startActivity<EditCategoryActivity>(EditCategoryActivity.EXTRA_CATEGORY_ID to category.id)
        } })

        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = CategoryAdapter(this, datasource.allCategories, object : OnItemClickListener {
            override fun onItemClick(item: Category?) {
                startActivity<EditCategoryActivity>(EditCategoryActivity.EXTRA_CATEGORY_ID to item!!.id)
            }
        })
        rv.adapter = adapter
    }
}
