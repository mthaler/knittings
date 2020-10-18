package com.mthaler.knittings.rowcounter

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_row_counter.*
import java.lang.Exception

class RowCounterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_row_counter)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = if (savedInstanceState != null) savedInstanceState.getLong(Extras.EXTRA_KNITTING_ID) else intent.getLongExtra(Extras.EXTRA_KNITTING_ID, -1L)

        val textViewRows = findViewById<TextView>(R.id.rows)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(RowCounterViewModel::class.java)
        viewModel.init(id)
        viewModel.rows.observe(this, Observer { rows ->
            textViewRows.text = rows.totalRows.toString()
        })

        val plusButton = findViewById<Button>(R.id.button_plus)
        plusButton.setOnClickListener {
            viewModel.incrementTotalRows()
        }

        val resetButton = findViewById<Button>(R.id.button_reset)
        resetButton.setOnClickListener {

        }

        val minusButton = findViewById<Button>(R.id.button_minus)
        minusButton.setOnClickListener {
            viewModel.decrementTotalRows()
        }
    }

    companion object {
        fun newIntent(context: Context, knittingID: Long): Intent  {
            val intent = Intent(context, RowCounterActivity::class.java)
            intent.putExtra(Extras.EXTRA_KNITTING_ID, knittingID)
            return intent
        }
    }
}