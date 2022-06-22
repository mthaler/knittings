package com.mthaler.knittings.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.ActivityCategoryListBinding

class CategoryListActivity : BaseActivity(), EditCategoryFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val f = CategoryListFragment.newInstance()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.category_list_container, f)
            ft.commit()
        }
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

    override fun categorySaved(categoryID: Long) {
        supportFragmentManager.popBackStack()
    }

    companion object {

        fun newIntent(context: Context): Intent = Intent(context, CategoryListActivity::class.java)
    }
}