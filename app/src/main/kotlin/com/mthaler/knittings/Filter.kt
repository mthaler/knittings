package com.mthaler.knittings

import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.utils.StringUtils.containsIgnoreCase
import java.io.Serializable

/**
 * Filter used for filtering the knitting list displayed to the user
 */
interface Filter : Serializable {
    /**
     * Filter the knitting list
     *
     * @param knittings knitting list
     * @return filtered knitting list
     */
    fun filter(knittings: List<Knitting>): List<Knitting>
}

/**
 * A filter that filters the project list by category
 *
 * @param category category used for filtering
 */
data class SingleCategoryFilter(val category: Category) : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.category == category }
}

/**
 * A filter that filters the project list by status
 *
 * @param status status used for filtering
 */
data class SingleStatusFilter(val status: Status) : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.status == status }
}

/**
 * A filter that filters the project list by checking if the title or the description contains the given text
 */
data class ContainsFilter(val text: String) : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { containsIgnoreCase(it.title, text) || containsIgnoreCase(it.description, text) }
}

/**
 * A combined filter that uses several filters to filter the project list
 *
 * @param filters list of filters
 */
data class CombinedFilter(val filters: List<Filter>) : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> {
        var result = knittings
        for (filter in filters) {
            result = filter.filter(result)
        }
        return result
    }

    companion object {

        // empty list that does no filtering
        val Empty = CombinedFilter(emptyList())
    }
}