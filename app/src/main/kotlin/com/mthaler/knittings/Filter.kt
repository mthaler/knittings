package com.mthaler.knittings

import com.mthaler.dbapp.filter.Filter
import com.mthaler.dbapp.model.Category
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.utils.StringUtils.containsIgnoreCase
import java.io.Serializable

/**
 * A filter that filters the project list by category
 *
 * @param category category used for filtering
 */
data class SingleCategoryFilter(val category: Category) : Filter<Knitting> {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.category == category }
}

/**
 * A filter that filters the project list by status
 *
 * @param status status used for filtering
 */
data class SingleStatusFilter(val status: Status) : Filter<Knitting> {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.status == status }
}

/**
 * A filter that filters the project list by checking if the title or the description contains the given text
 */
data class ContainsFilter(val text: String) : Filter<Knitting> {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { containsIgnoreCase(it.title, text) || containsIgnoreCase(it.description, text) }
}