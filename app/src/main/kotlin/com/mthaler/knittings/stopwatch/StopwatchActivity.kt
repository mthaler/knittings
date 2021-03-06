package com.mthaler.knittings.stopwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.ActivityStopwatchBinding
import com.mthaler.knittings.model.Knitting

/**
 * StopWatchActivity shows a stopwatch that can be used to measure the time the user is working on a knitting
 */
class StopwatchActivity : BaseActivity() {

    private lateinit var binding: ActivityStopwatchBinding
    private var knittingID: Long = Knitting.EMPTY.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopwatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the knitting for the stopwatch
        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            knittingID = id
        } else {
            error("Could not get knitting id")
        }

        if (savedInstanceState == null) {
            val f = StopwatchFragment.newInstance(knittingID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.stopwatch_container, f)
            ft.commit()
        }
    }

    companion object {

        fun newIntent(context: Context, knittingID: Long): Intent {
            val intent = Intent(context, StopwatchActivity::class.java)
            intent.putExtra(EXTRA_KNITTING_ID, knittingID)
            return intent
        }
    }
}
