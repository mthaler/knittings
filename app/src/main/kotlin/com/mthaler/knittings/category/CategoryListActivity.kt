package com.mthaler.knittings.category

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Category

class CategoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        val categories = ArrayList<Category>()
        categories.add(Category(0,"Socks", Color.RED))
        categories.add(Category(1,"Pullovers", Color.BLUE))
        categories.add(Category(2,"Gloves", Color.GREEN))
        categories.add(Category(3,"Coat", Color.YELLOW))

        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = CategoryAdapter(categories)
        rv.adapter = adapter
    }
}
