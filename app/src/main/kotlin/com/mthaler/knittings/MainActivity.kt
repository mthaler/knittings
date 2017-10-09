package com.mthaler.knittings

import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.*
import com.mthaler.knittings.BuildConfig;

/**
 * The main activity that gets displayed when the app is started.
 * It displays a list of knittings. The user can add new knittings or
 * edit existing ones
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab_create_add_knitting) as FloatingActionButton
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
                alert("Idea and concept: Ulrike Jordan,\n\nProgramming: Michael Thaler") {
                    title = resources.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME
                    positiveButton("OK") {}
                }.show()
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
