package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_project_count.*
import android.widget.ArrayAdapter
import java.util.*
import android.widget.Spinner
import android.widget.TextView
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

        val textViewProjectCount = findViewById<TextView>(R.id.projectCount)

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
        years.add("All")
        years.reverse()
        val yearAdapter = ArrayAdapter(this, R.layout.my_spinner, years)
        val spinYear = findViewById<Spinner>(R.id.year_spinner)
        spinYear.adapter = yearAdapter
        spinYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val knittings = datasource.allKnittings
                if (position == 0) {
                    textViewProjectCount.text = Integer.toString(knittings.size)
                } else {
                    val year = Integer.parseInt(years[position])
                    textViewProjectCount.text = Integer.toString(knittings.count {
                        val c = Calendar.getInstance()
                        c.time = it.started
                        year == c.get(Calendar.YEAR)
                    })
                }
            }
        }

        val categories = datasource.allCategories
        categories.sortedBy { it.name }
        val categoryNames = listOf(getString(R.string.filter_show_all)) + categories.map { it.name }.toList()
        val categoryAdapter = ArrayAdapter(this, R.layout.my_spinner, categoryNames)
        val spinCategory = findViewById<Spinner>(R.id.category_spinner)
        spinCategory.adapter = categoryAdapter
        spinCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val knittings = datasource.allKnittings
                if (position == 0) {
                    textViewProjectCount.text = Integer.toString(knittings.size)
                } else {
                    val categoryName = categoryNames[position]
                    textViewProjectCount.text = Integer.toString(knittings.count { it.category != null && it.category.name == categoryName })
                }
            }
        }
    }
}
