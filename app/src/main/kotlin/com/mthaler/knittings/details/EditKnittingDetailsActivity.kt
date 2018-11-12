package com.mthaler.knittings.details

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import kotlinx.android.synthetic.main.activity_edit_knitting_details.*

/**
 * EditKnittingDetailsActivity is used to edit knitting details
 */
class EditKnittingDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_knitting_details)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            // initialize the edit knitting details fragment with the knitting it should display
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_edit_knitting_details) as EditKnittingDetailsFragment
            val knitting = datasource.getKnitting(id)
            fragment.init(knitting)
        }
    }

    companion object {
        const val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
