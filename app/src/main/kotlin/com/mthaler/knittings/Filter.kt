package com.mthaler.knittings

import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Knitting
import java.io.Serializable

/**
 * Filter used for filtering the knitting list displayed to the user
 */
interface Filter : Serializable {
    /**
     * Filter the knitting list
     *
     * @param knitting list
     * @return filtered knitting list
     */
    fun filter(knittings: List<Knitting>): List<Knitting>
}

/**
 * A filter that does no filtering: it just returns the given knitting list
 */
object NoFilter : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings
}

/**
 * A filter that filters the knitting list by category
 *
 * @param category used for filtering
 */
data class SingleCategoryFilter(val category: Category) : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.category == category }
}
