package com.mthaler.knittings.category

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.database.Extras
import com.mthaler.knittings.database.Extras.EXTRA_OWNER_ID
import com.mthaler.knittings.databinding.ActivitySelectCategoryBinding
import com.mthaler.knittings.model.Category

class SelectCategoryActivity : BaseActivity(), EditCategoryFragment.OnFragmentInteractionListener {

    private var ownerID = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the category that should be displayed.
        ownerID = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_OWNER_ID) else intent.getLongExtra(EXTRA_OWNER_ID, -1L)

        if (savedInstanceState == null) {
            val f = CategoryListFragment.newInstance()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.select_category_container, f)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_OWNER_ID, ownerID)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        val f =  supportFragmentManager.findFragmentById(R.id.select_category_container)
        if (f is EditCategoryFragment) {
            f.onBackPressed { _ ->
                val i = Intent()
                i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategoryID())
                setResult(Activity.RESULT_OK, i)
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            val f =  supportFragmentManager.findFragmentById(R.id.select_category_container)
            if (f is EditCategoryFragment) {
                f.onBackPressed {
                    if (f.getCategoryID() == Category.EMPTY.id) {
                        finish()
                    } else {
                        val i = Intent()
                        i.putExtra(EXTRA_OWNER_ID, ownerID)
                        i.putExtra(Extras.EXTRA_CATEGORY_ID, f.getCategoryID())
                        setResult(Activity.RESULT_OK, i)
                        finish()
                    }
                }
            } else {
                finish()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun categorySaved(categoryID: Long) {
        if (categoryID == Category.EMPTY.id) {
            finish()
        } else {
            val i = Intent()
            i.putExtra(EXTRA_OWNER_ID, ownerID)
            i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }

    companion object {

        fun newIntent(context: Context, ownerID: Long): Intent {
            val intent = Intent(context, SelectCategoryActivity::class.java)
            intent.putExtra(EXTRA_OWNER_ID, ownerID)
            return intent
        }
    }
}
