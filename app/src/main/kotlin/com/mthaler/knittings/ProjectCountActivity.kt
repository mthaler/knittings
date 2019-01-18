package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_project_count.*
import android.widget.ArrayAdapter
import java.util.*
import android.widget.Spinner
import com.mthaler.knittings.database.datasource

class ProjectCountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_count)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val knittings = datasource.allKnittings
        knittings.sortBy { it.started }
        val oldest = knittings.firstOrNull()

        val years = ArrayList<String>()
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        if (oldest != null) {
            val c = Calendar.getInstance()
            c.time = oldest.started
            for (i in c.get(Calendar.YEAR)..thisYear) {
                years.add(Integer.toString(i))
            }
        } else {
            years.add(Integer.toString(thisYear))
        }
        years.reverse()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        val spinYear = findViewById(R.id.year_spinner) as Spinner
        spinYear.setAdapter(adapter)
    }
}
