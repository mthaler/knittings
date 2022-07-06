package com.mthaler.knittings.filter

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.containsIgnoreCase

/**
 * A filter that filters the project list by checking if the title or the description contains the given text
 */
data class ContainsFilter(val text: String) : Filter<Knitting> {

    override fun filter(items: List<Knitting>): List<Knitting> = items.filter { it.title.containsIgnoreCase(text) || it.description.containsIgnoreCase(text) }
}