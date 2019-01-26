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

class SelectCategoryActivity : AppCompatActivity(), SelectCategoryFragment.OnCreateCategoryListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_category)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the category that should be displayed.
        val knittingID = intent.getLongExtra(EXTRA_KNITTING_ID, -1L)

        if (savedInstanceState == null) {
            val f = SelectCategoryFragment.newInstance(knittingID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.select_category_container, f)
            //ft.addToBackStack(null)
            ft.commit()
        }
    }

    override fun createCategory() {
        val category = datasource.createCategory("", null)
        val i = Intent(this, EditCategoryActivity::class.java)
        i.putExtra(Extras.EXTRA_CATEGORY_ID, category.id)
        startActivityForResult(i, REQUEST_EDIT_CATEGORY)
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

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with, the resultCode it returned,
     * and any additional data from it. The resultCode will be RESULT_CANCELED if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_EDIT_CATEGORY) {
            data?.let {
                val categoryID = it.getLongExtra(Extras.EXTRA_CATEGORY_ID, -1)
                val i = Intent()
                i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
                setResult(Activity.RESULT_OK, i)
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_EDIT_CATEGORY = 0
    }
}
