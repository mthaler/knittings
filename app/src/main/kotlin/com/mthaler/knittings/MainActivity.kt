package com.mthaler.knittings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.mthaler.knittings.category.CategoryListActivity
import com.mthaler.knittings.dropbox.DropboxExportActivity
import com.mthaler.knittings.dropbox.DropboxImportActivity
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.activity_main.*
import com.mthaler.knittings.database.datasource

/**
 * The main activity that gets displayed when the app is started.
 * It displays a list of knittings. The user can add new knittings or
 * edit existing ones
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add toolbar to activity
        setSupportActionBar(toolbar)

        // set on click handler of floating action button that creates a new knitting
        fab_create_add_knitting.setOnClickListener {
            val knittingListView = supportFragmentManager.findFragmentById(R.id.fragment_knitting_list) as KnittingListView
            knittingListView.addKnitting()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knittings_list, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_about -> {
                showAboutDialog()
                true
            }
            R.id.menu_item_sort -> {
                val knittingListView = supportFragmentManager.findFragmentById(R.id.fragment_knitting_list) as KnittingListView
                val listItems = arrayOf(getString(R.string.sorting_newest_first), getString(R.string.sorting_oldest_first), getString(R.string.sorting_alphabetical))
                val builder = AlertDialog.Builder(this)
                val checkedItem = when(knittingListView.getSorting()) {
                    Sorting.NewestFirst -> 0
                    Sorting.OldestFirst -> 1
                    Sorting.Alphabetical -> 2
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when(which) {
                    0 -> knittingListView.setSorting(Sorting.NewestFirst)
                    1 -> knittingListView.setSorting(Sorting.OldestFirst)
                    2 -> knittingListView.setSorting(Sorting.Alphabetical)
                  }
                    knittingListView.updateKnittingList()
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.menu_item_filter -> {
                val knittingListView = supportFragmentManager.findFragmentById(R.id.fragment_knitting_list) as KnittingListView
                val categories = datasource.allCategories
                categories.sortedBy { it.name }
                val listItems = (listOf("All") + categories.map { it.name }.toList()).toTypedArray()
                val builder = AlertDialog.Builder(this)
                val checkedItem = 0
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when(which) {
                    0 -> {}
                    else -> {}
                }
                    knittingListView.updateKnittingList()
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_dropbox_export -> {
                startActivity<DropboxExportActivity>()
            }
            R.id.nav_dropbox_import -> {
                startActivity<DropboxImportActivity>()
            }
            R.id.nav_edit_categories -> {
                startActivity<CategoryListActivity>()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Shows the about dialog that displays the app name, version and some additional information
     */
    private fun showAboutDialog() {
        @SuppressLint("InflateParams")
        val v = layoutInflater.inflate(R.layout.dialog_about, null)
        val appName = v.find<TextView>(R.id.about_app_name)
        appName.text = (appName.text.toString() + " " + BuildConfig.VERSION_NAME)
        val b = AlertDialog.Builder(this)
        b.setView(v)
        b.setPositiveButton(android.R.string.ok ) { diag, i -> diag.dismiss()}
        b.create().show()
    }
}
