package com.mthaler.knittings.filter

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.utils.StringUtils

/**
 * A filter that filters the project list by checking if the title or the description contains the given text
 */
data class ContainsFilter(val text: String) : Filter<Knitting> {

    override fun filter(projects: List<Knitting>): List<Knitting> = projects.filter { StringUtils.containsIgnoreCase(it.title, text) || StringUtils.containsIgnoreCase(it.description, text) }
}