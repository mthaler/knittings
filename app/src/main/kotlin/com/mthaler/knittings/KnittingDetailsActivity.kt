package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import org.jetbrains.anko.AnkoLogger
import com.mthaler.knittings.database.datasource

/**
 * Activity that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting_details)

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
        } else {
            error("Could not get knitting id")
        }

        // start edit knitting details fragment if the user clicks the floating action button
        val fab = findViewById<FloatingActionButton>(R.id.edit_knitting_details)
        fab.setOnClickListener {
        }
    }

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
