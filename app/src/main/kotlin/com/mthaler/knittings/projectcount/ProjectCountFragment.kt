package com.mthaler.knittings.projectcount

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentProjectCountBinding
import com.mthaler.knittings.model.Project
import java.util.Calendar

class ProjectCountFragment : Fragment() {

    private var _binding: FragmentProjectCountBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // get all knittings from database
        val ds = (requireContext().applicationContext as com.mthaler.knittings.DatabaseApplication).getProjectsDataSource()
        val projects = ds.allProjects

        _binding = FragmentProjectCountBinding.inflate(inflater, container, false)
        val view = binding.root

        val years = createYearsList(projects)
        val categoryNames = createCategoryNamesList()

        val yearAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, years)
        binding.yearSpinner.adapter = yearAdapter
        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val year = if (position == 0) null else Integer.parseInt(years[position])
                val p = binding.categorySpinner.selectedItemPosition
                val categoryName = if (p == 0) null else categoryNames[p]
                val projectCount = getProjectCount(projects, year, categoryName)
                binding.projectCount.text = Integer.toString(projectCount) + " / " + Integer.toString(projects.size)
                val percent = if (projects.size > 0) 100.0 * projectCount / projects.size else 0.0
                binding.progressBarCircle.progress = percent.toFloat()
            }
        }

        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, categoryNames)
        binding.categorySpinner.adapter = categoryAdapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val p = binding.yearSpinner.selectedItemPosition
                val year = if (p == 0) null else Integer.parseInt(years[p])
                val categoryName = if (position == 0) null else categoryNames[position]
                val projectCount = getProjectCount(projects, year, categoryName)
                binding.projectCount.text = Integer.toString(projectCount) + " / " + Integer.toString(projects.size)
                val percent = if (projects.size > 0) 100.0 * projectCount / projects.size else 0
                binding.progressBarCircle.progress = percent.toFloat()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Creates a list of years starting with the year of the oldest knitting until the current year
     * All is added as the first element of the list
     *
     * @param projects list of all projects
     * @return list of years
     */
    private fun createYearsList(projects: List<Project>): List<String> {
        val oldest = projects.minByOrNull { it.started.time }
        val years = ArrayList<String>()
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        if (oldest != null) {
            val c = Calendar.getInstance()
            c.time = oldest.started
            for (i in c.get(Calendar.YEAR)..thisYear) {
                years.add(i.toString())
            }
        } else {
            years.add(thisYear.toString())
        }
        //years.add(getString(R.string.filter_show_all))
        years.reverse()
        return years
    }

    /**
     * Creates a list of categories. All is added as the first element of the list
     *
     * @return list of categories
     */
    private fun createCategoryNamesList(): List<String> {
        val ds = (requireContext().applicationContext as com.mthaler.knittings.DatabaseApplication).getCategoryDataSource()
        val categories = ds.allCategories.sortedBy { it.name.toLowerCase() }
        return listOf(getString(R.string.filter_show_all)) + categories.map { it.name }.toList()
    }

    companion object {

        /**
         * Gets the project count for the given year and category name
         *
         * @param projects list of all projects
         * @param year year to get the project count for or null for all years
         * @param categoryName name of the category to get the project count for or null for all categories
         */
        private fun getProjectCount(projects: List<Project>, year: Int?, categoryName: String?): Int {
            if (year == null && categoryName == null) {
                return projects.size
            } else if (year != null && categoryName == null) {
                return projects.count {
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
                return projects.count {
                    val category = it.category
                    category != null && category.name == categoryName }
            } else {
                return projects.count {
                    val finished = it.finished
                    if (finished != null) {
                        val c = Calendar.getInstance()
                        c.time = finished
                        val category = it.category
                        year == c.get(Calendar.YEAR) && category != null && category.name == categoryName
                    } else {
                        false
                    }
                }
            }
        }
    }
}