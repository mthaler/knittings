package com.mthaler.knittings.filter

import com.mthaler.knittings.model.Project
import com.mthaler.knittings.utils.StringUtils

/**
 * A filter that filters the project list by checking if the title or the description contains the given text
 */
data class ContainsFilter<T : Project>(val text: String) : Filter<T> {

    override fun filter(projects: List<T>): List<T> = projects.filter { StringUtils.containsIgnoreCase(it.title, text) || StringUtils.containsIgnoreCase(it.description, text) }
}