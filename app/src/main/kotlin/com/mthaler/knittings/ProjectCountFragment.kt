package com.mthaler.knittings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
import java.util.Calendar

class ProjectCountFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_project_count, container, false)

        // get all knittings from database
        val knittings = datasource.allKnittings

        val years = createYearsList(knittings)
        val categoryNames = createCategoryNamesList()

        val textViewProjectCount = v.findViewById<TextView>(R.id.projectCount)
        val spinYear = v.findViewById<Spinner>(R.id.year_spinner)
        val spinCategory = v.findViewById<Spinner>(R.id.category_spinner)
        val progressBarCircle = v.findViewById<ProgressBar>(R.id.progressBarCircle)

        val yearAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, years)
        spinYear.adapter = yearAdapter
        spinYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val year = if (position == 0) null else Integer.parseInt(years[position])
                val p = spinCategory.selectedItemPosition
                val categoryName = if (p == 0) null else categoryNames[p]
                val projectCount = getProjectCount(knittings, year, categoryName)
                textViewProjectCount.text = Integer.toString(projectCount) + " / " + Integer.toString(knittings.size)
                val percent = if (knittings.size > 0) 100 * projectCount / knittings.size else 0
                progressBarCircle.progress = percent
            }
        }

        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, categoryNames)
        spinCategory.adapter = categoryAdapter
        spinCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val p = spinYear.selectedItemPosition
                val year = if (p == 0) null else Integer.parseInt(years[p])
                val categoryName = if (position == 0) null else categoryNames[position]
                val projectCount = getProjectCount(knittings, year, categoryName)
                textViewProjectCount.text = Integer.toString(projectCount) + " / " + Integer.toString(knittings.size)
                val percent = if (knittings.size > 0) 100 * projectCount / knittings.size else 0
                progressBarCircle.progress = percent
            }
        }

        return v
    }

    /**
     * Creates a list of years starting with the year of the oldest knitting until the current year
     * All is added as the first element of the list
     *
     * @param knittings list of all knittings
     * @return list of years
     */
    private fun createYearsList(knittings: List<Knitting>): List<String> {
        val oldest = knittings.minBy { it.started.time }
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
        return years
    }

    /**
     * Creates a list of categories. All is added as the first element of the list
     *
     * @return list of categories
     */
    private fun createCategoryNamesList(): List<String> {
        val categories = datasource.allCategories
        categories.sortBy { it.name }
        return listOf(getString(R.string.filter_show_all)) + categories.map { it.name }.toList()
    }

    companion object {

        /**
         * Gets the project count for the given year and category name
         *
         * @param knittings list of all knittings
         * @param year year to get the project count for or null for all years
         * @param categoryName name of the category to get the project count for or null for all categories
         */
        private fun getProjectCount(knittings: List<Knitting>, year: Int?, categoryName: String?): Int {
            if (year == null && categoryName == null) {
                return knittings.size
            } else if (year != null && categoryName == null) {
                return knittings.count {
                    val finished = it.finished
                    if (finished != null) {
                        val c = Calendar.getInstance()
                        c.time = finished
                        year == c.get(Calendar.YEAR)
                    } else {
                        false
                    }
                }
            } else if (year == null && categoryName != null) {
                return knittings.count { it.category != null && it.category.name == categoryName }
            } else {
                return knittings.count {
                    val finished = it.finished
                    if (finished != null) {
                        val c = Calendar.getInstance()
                        c.time = finished
                        year == c.get(Calendar.YEAR) && it.category != null && it.category.name == categoryName
                    } else {
                        false
                    }
                }
            }
        }
    }
}
