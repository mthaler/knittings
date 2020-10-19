package com.mthaler.knittings.rowcounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Knitting
import kotlinx.android.synthetic.main.activity_row_counter.*

class RowCounterActivity : BaseActivity() {

    private var knittingID: Long = Knitting.EMPTY.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_row_counter)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        knittingID = if (savedInstanceState != null) savedInstanceState.getLong(Extras.EXTRA_KNITTING_ID) else intent.getLongExtra(Extras.EXTRA_KNITTING_ID, -1L)

        val textViewRows = findViewById<TextView>(R.id.rows)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(RowCounterViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.rows.observe(this, Observer { rows ->
            textViewRows.text = rows.totalRows.toString()
        })

        val plusButton = findViewById<ImageButton>(R.id.button_plus)
        plusButton.setOnClickListener {
            viewModel.incrementTotalRows()
        }

        val resetButton = findViewById<ImageButton>(R.id.button_reset)
        resetButton.setOnClickListener {

        }

        val minusButton = findViewById<ImageButton>(R.id.button_minus)
        minusButton.setOnClickListener {
            viewModel.decrementTotalRows()
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(Extras.EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(savedInstanceState)
    }

    companion object {
        fun newIntent(context: Context, knittingID: Long): Intent  {
            val intent = Intent(context, RowCounterActivity::class.java)
            intent.putExtra(Extras.EXTRA_KNITTING_ID, knittingID)
            return intent
        }
    }
}