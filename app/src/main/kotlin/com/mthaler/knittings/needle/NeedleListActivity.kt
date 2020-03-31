package com.mthaler.knittings.needle

import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Needle
import kotlinx.android.synthetic.main.activity_needle_list.*

class NeedleListActivity : BaseActivity(), NeedleListFragment.OnFragmentInteractionListener {

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
        setContentView(R.layout.activity_needle_list)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val f = NeedleListFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.needle_list_container, f)
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
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val fm = supportFragmentManager
                val f = fm.findFragmentById(R.id.needle_list_container)
                if (f is EditNeedleFragment) {
                    fm.popBackStack()
                } else {
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun createNeedle() {
        val needle = datasource.addNeedle(Needle())
        val f = EditNeedleFragment.newInstance(needle.id)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.needle_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun needleClicked(needleID: Long) {
        val f = EditNeedleFragment.newInstance(needleID)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.needle_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }
}
