package com.mthaler.knittings.category

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_select_category.*
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID

class SelectCategoryActivity : AppCompatActivity() {

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
}
