package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.AnkoLogger
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.alert
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Activity that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsActivity : AppCompatActivity(), AnkoLogger {

    private var knittingID: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting_details)

        val toolbar = find<Toolbar>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(KnittingDetailsActivity.EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            // initialize the knitting details fragment with the knitting it should display
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_knitting_details) as KnittingDetailsFragment
            if (fragment != null) {
                val knitting = datasource.getKnitting(id)
                fragment.init(knitting)
            } else {
                error("Could not get knitting details fragment")
            }

            // start edit knitting details fragment if the user clicks the floating action button
            val fab = findViewById<FloatingActionButton>(R.id.edit_knitting_details)
            fab.setOnClickListener {
                startActivity< EditKnittingDetailsActivity>(EditKnittingDetailsActivity.EXTRA_KNITTING_ID to id)
            }
            knittingID = id
        } else {
            error("Could not get knitting id")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knitting_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_delete_knitting -> {
                // show alert asking user to confirm that knitting should be deleted
                alert {
                    title = resources.getString(R.string.delete_knitting_dialog_title)
                    message = resources.getString(R.string.delete_knitting_dialog_question)
                    positiveButton(resources.getString(R.string.delete_knitting_dialog_delete_button)) {
                        val knitting = datasource.getKnitting(knittingID)
                        // delete all photos from the database
                        datasource.deleteAllPhotos(knitting)
                        // delete database entry
                        datasource.deleteKnitting(knitting)
                        finish()
                    }
                    negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
                }.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
