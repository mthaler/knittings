package com.mthaler.knittings.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.databinding.ActivityKnittingDetailsBinding
import com.mthaler.knittings.model.Knitting

/**
 * Activity that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsActivity : BaseActivity(), KnittingDetailsFragment.OnFragmentInteractionListener {

    // id of the displayed knitting
    private var knittingID: Long = Knitting.EMPTY.id
    private var editOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKnittingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed. If the application was destroyed because e.g. the device configuration changed
        // because the device was rotated we use the knitting id from the saved instance state. Otherwise we use the id passed to the intent
        knittingID = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, Knitting.EMPTY.id)
        editOnly = intent.getBooleanExtra(EXTRA_EDIT_ONLY, false)
        if (savedInstanceState == null) {
            val navController = findNavController(R.id.nav_host_fragment)
            if (editOnly) {
                navController.navigate(R.id.editKnittingDetailsFragment)
            } else {
                navController.navigate(R.id.knittingDetailsFragment)
                //val f = KnittingDetailsFragment.newInstance(knittingID)
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

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        if (!navController.navigateUp()) {
            super.onNavigateUp()
        }
    }

    override fun editKnitting(id: Long) {
        val bundle = bundleOf(EXTRA_KNITTING_ID to id, EXTRA_EDIT_ONLY to true)
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.editKnittingDetailsFragment, bundle)
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