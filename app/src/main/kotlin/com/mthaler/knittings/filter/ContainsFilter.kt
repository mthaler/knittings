package com.mthaler.knittings.filter

import com.mthaler.dbapp.filter.Filter
import com.mthaler.dbapp.utils.StringUtils
import com.mthaler.knittings.model.Knitting

/**
 * A filter that filters the project list by checking if the title or the description contains the given text
 */
data class ContainsFilter(val text: String) : Filter<Knitting> {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { StringUtils.containsIgnoreCase(it.title, text) || StringUtils.containsIgnoreCase(it.description, text) }
}