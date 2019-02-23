package com.mthaler.knittings.needle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import kotlinx.android.synthetic.main.activity_needle_list.*

class NeedleListActivity : AppCompatActivity(), NeedleListFragment.OnFragmentInteractionListener {

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
        val needle = datasource.createNeedle()
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

    override fun needleLongClicked(needleID : Long) {
        startSupportActionMode(object : ActionMode.Callback {

            /**
             * Called to report a user click on an action button.
             *
             * @param mode The current ActionMode
             * @param menu The item that was clicked
             * @return true if this callback handled the event, false if the standard MenuItem invocation should continue.
             */
            override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
               when(menu?.itemId) {
                   R.id.action_delete -> {
                       DeleteNeedleDialog.create(this@NeedleListActivity, {  datasource.deleteNeedle(datasource.getNeedle(needleID)) }).show()
                       mode?.finish()
                       return true
                   }
                   else -> {
                       return false
                   }
               }
            }

            /**
             * Called when action mode is first created. The menu supplied will be used to generate action buttons for the action mode.
             *
             * @param mode The current ActionMode
             * @param menu  Menu used to populate action buttons
             * @return true if the action mode should be created, false if entering this mode should be aborted.
             */
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val inflater = mode?.getMenuInflater()
                inflater?.inflate(R.menu.needle_list_action, menu)
                return true
            }

            /**
             * Called to refresh an action mode's action menu whenever it is invalidated.
             *
             * @param mode The current ActionMode
             * @param menu  Menu used to populate action buttons
             */
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }

            /**
             * Called when an action mode is about to be exited and destroyed.
             *
             * @param mode The current ActionMode
             */
            override fun onDestroyActionMode(mode: ActionMode?) {
            }
        })
    }
}
