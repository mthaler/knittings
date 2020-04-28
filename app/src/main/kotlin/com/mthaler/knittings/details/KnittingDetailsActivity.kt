package com.mthaler.knittings.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.model.Knitting
import kotlinx.android.synthetic.main.activity_knitting_details.*

/**
 * Activity that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsActivity : BaseActivity(), KnittingDetailsFragment.OnFragmentInteractionListener {

    // id of the displayed knitting
    private var knittingID: Long = Knitting.EMPTY.id
    private var editOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting_details)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed. If the application was destroyed because e.g. the device configuration changed
        // because the device was rotated we use the knitting id from the saved instance state. Otherwise we use the id passed to the intent
        knittingID = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, Knitting.EMPTY.id)
        editOnly = intent.getBooleanExtra(EXTRA_EDIT_ONLY, false)
        if (savedInstanceState == null) {
            if (editOnly) {
                val f = EditKnittingDetailsFragment.newInstance(knittingID, editOnly)
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.add(R.id.knitting_details_container, f)
                ft.commit()
            } else {
                val f = KnittingDetailsFragment.newInstance(knittingID)
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.add(R.id.knitting_details_container, f)
                ft.commit()
            }
        } else {
            if (savedInstanceState.containsKey(EXTRA_EDIT_ONLY)) {
                editOnly = savedInstanceState.getBoolean(EXTRA_EDIT_ONLY)
            }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_KNITTING_ID, knittingID)
        savedInstanceState.putBoolean(EXTRA_EDIT_ONLY, editOnly)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val fm = supportFragmentManager
                val f = fm.findFragmentById(R.id.knitting_details_container)
                if (f is EditKnittingDetailsFragment) {
                    f.onBackPressed()
                } else {
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        val f = fm.findFragmentById(R.id.knitting_details_container)
        if (f is EditKnittingDetailsFragment) {
            f.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun editKnitting(id: Long) {
        val f = EditKnittingDetailsFragment.newInstance(knittingID, editOnly)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.knitting_details_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {

        const val EXTRA_EDIT_ONLY = "com.mthaler.knittings.edit_only"

        fun newIntent(context: Context, knittingID: Long, editOnly: Boolean): Intent {
            val intent = Intent(context, KnittingDetailsActivity::class.java)
            intent.putExtra(EXTRA_KNITTING_ID, knittingID)
            intent.putExtra(EXTRA_EDIT_ONLY, editOnly)
            return intent
        }
    }
}
