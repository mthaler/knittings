package com.mthaler.knittings

import android.app.AlertDialog
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import org.jetbrains.anko.*

/**
 * The main activity that gets displayed when the app is started.
 * It displays a list of knittings. The user can add new knittings or
 * edit existing ones
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add toolbar to activity
        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set on click handler of floating action button that creates a new knitting
        val fab = find<FloatingActionButton>(R.id.fab_create_add_knitting)
        fab.setOnClickListener {
            val knittingListView = supportFragmentManager.findFragmentById(R.id.fragment_knitting_list) as KnittingListView
            knittingListView.addKnitting()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knittings_list, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_about -> {
                showAboutDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows the about dialog that displays the app name, version and some additional information
     */
    private fun showAboutDialog() {
        val v = layoutInflater.inflate(R.layout.dialog_about, null)
        val appName = v.find<TextView>(R.id.about_app_name)
        appName.text = (appName.text.toString() + " " + BuildConfig.VERSION_NAME)
        val b = AlertDialog.Builder(this)
        b.setView(v)
        b.setPositiveButton(android.R.string.ok ) { diag, i -> diag.dismiss()}
        b.create().show()
    }
}
