package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.find

class EditKnittingDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_knitting_details)

        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EditKnittingDetailsActivity.EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            // initialize the edit knitting details fragment with the knitting it should display
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_edit_knitting_details) as EditKnittingDetailsFragment
            if (fragment != null) {
                val knitting = datasource.getKnitting(id)
                fragment.init(knitting)
            } else {
                error("Could not get knitting details fragment")
            }
        }
    }

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
