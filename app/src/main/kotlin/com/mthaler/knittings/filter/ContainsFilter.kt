package com.mthaler.knittings.filter

import com.mthaler.dbapp.filter.Filter
import com.mthaler.dbapp.model.Project
import com.mthaler.dbapp.utils.StringUtils
import com.mthaler.knittings.model.Knitting

/**
 * A filter that filters the project list by checking if the title or the description contains the given text
 */
data class ContainsFilter<T : Project>(val text: String) : Filter<T> {

    override fun filter(knittings: List<T>): List<T> = knittings.filter { StringUtils.containsIgnoreCase(it.title, text) || StringUtils.containsIgnoreCase(it.description, text) }
}