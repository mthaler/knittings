package com.mthaler.knittings.filter

import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Project

/**
 * A filter that filters the project list by category
 *
 * @param category category used for filtering
 */
data class SingleCategoryFilter<T : Project>(val category: Category) : Filter<T> {

    override fun filter(projects: List<T>): List<T> = projects.filter { it.category == category }
}