package com.mthaler.knittings.rowcounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.ActivityRowCounterBinding
import com.mthaler.knittings.model.Knitting

class RowCounterActivity : BaseActivity() {

    private var knittingID: Long = Knitting.EMPTY.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRowCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        knittingID = if (savedInstanceState != null) savedInstanceState.getLong(Extras.EXTRA_KNITTING_ID) else intent.getLongExtra(Extras.EXTRA_KNITTING_ID, -1L)

        if (savedInstanceState == null) {
            val f = RowCounterFragment.newInstance(knittingID)
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.row_counter_container, f)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(Extras.EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val f = supportFragmentManager.findFragmentById(R.id.knitting_details_container)
                upIntent.putExtra(Extras.EXTRA_KNITTING_ID, knittingID)
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        fun newIntent(context: Context, knittingID: Long): Intent  {
            val intent = Intent(context, RowCounterActivity::class.java)
            intent.putExtra(Extras.EXTRA_KNITTING_ID, knittingID)
            return intent
        }
    }
}