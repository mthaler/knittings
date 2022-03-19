package com.mthaler.knittings.projectcount

import com.mthaler.knittings.model.Project
import java.util.*

class ProjectCountViewModel {

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
}