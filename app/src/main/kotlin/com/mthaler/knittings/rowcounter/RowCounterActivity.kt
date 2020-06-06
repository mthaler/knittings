package com.mthaler.knittings.rowcounter

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_project_count.*

class RowCounterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_row_counter)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val textViewRows = findViewById<TextView>(R.id.rows)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(RowCounterViewModel::class.java)
        viewModel.rows.observe(this, Observer { rows ->
            textViewRows.setText(rows.toString())
        })

        val plusButton = findViewById<Button>(R.id.button_plus)
        plusButton.setOnClickListener {
            viewModel.rows.value = viewModel.rows.value!! + 1
        }

        val minusButton = findViewById<Button>(R.id.button_minus)
        minusButton.setOnClickListener {
            viewModel.rows.value = viewModel.rows.value!! - 1
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, RowCounterActivity::class.java)
    }
}