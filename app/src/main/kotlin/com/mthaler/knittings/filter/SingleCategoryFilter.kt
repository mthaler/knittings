package com.mthaler.knittings.filter

import com.mthaler.dbapp.filter.Filter
import com.mthaler.dbapp.model.Category
import com.mthaler.knittings.model.Knitting

/**
 * A filter that filters the project list by category
 *
 * @param category category used for filtering
 */
data class SingleCategoryFilter(val category: Category) : Filter<Knitting> {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.category == category }
}