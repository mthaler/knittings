package com.mthaler.knittings.filter

import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Knitting

/**
 * A filter that filters the project list by category
 *
 * @param category category used for filtering
 */
data class SingleCategoryFilter(val category: Category) : Filter<Knitting> {

    override fun filter(items: List<Knitting>): List<Knitting> = items.filter { it.category == category }
}