package com.mthaler.knittings.rowcounter

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_project_count.*

class RowCounterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_row_counter)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val plusButton = findViewById<Button>(R.id.button_plus)
        plusButton.setOnClickListener {

        }

        val minusButton = findViewById<Button>(R.id.button_minus)
        minusButton.setOnClickListener {

        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, RowCounterActivity::class.java)
    }
}