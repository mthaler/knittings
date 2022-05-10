package com.mthaler.knittings.projectcount

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mthaler.knittings.MyApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Project
import java.util.*

class ProjectCountViewModel(application: Application): AndroidViewModel(application!!) {

    /**
     * Gets the project count for the given year and category name
     *
     * @param projects list of all projects
     * @param year year to get the project count for or null for all years
     * @param categoryName name of the category to get the project count for or null for all categories
     */
    fun getProjectCount(projects: List<Project>, year: Int?, categoryName: String?): Int {
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

    /**
     * Creates a list of categories. All is added as the first element of the list
     *
     * @return list of categories
     */
    fun createCategoryNamesList(): List<String> {
        val ds = (getApplication<MyApplication>().applicationContext as com.mthaler.knittings.DatabaseApplication).getCategoryDataSource()
        val categories = ds.allCategories.sortedBy { it.name.toLowerCase() }
        return listOf(getApplication<MyApplication>().resources.getString(R.string.project_count_category_all)) + categories.map { it.name }.toList()
    }

    /**
     * Creates a list of years starting with the year of the oldest knitting until the current year
     * All is added as the first element of the list
     *
     * @param projects list of all projects
     * @return list of years
     */
    fun createYearsList(projects: List<Project>): List<String> {
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
        years.add(getApplication<MyApplication>().resources.getString(R.string.project_count_year_all))
        years.reverse()
        return years
    }
}