package com.mthaler.knittings.needle

import com.mthaler.knittings.model.Needle
import java.io.Serializable

/**
 * Filter used for filtering the needle list displayed to the user
 */
interface Filter : Serializable {
    /**
     * Filter the knitting list
     *
     * @param needles needle list
     * @return filtered needle list
     */
    fun filter(needles: List<Needle>): List<Needle>
}

/**
 * A filter that does no filtering: it just returns the given knitting list
 */
object NoFilter : Filter {

    override fun filter(needles: List<Needle>): List<Needle> = needles
}

/**
 * A filter that filters the project list by category
 *
 * @param category used for filtering
 */
data class SingleTypeFilter(val type: String) : Filter {

    override fun filter(needles: List<Needle>): List<Needle> = needles.filter { it.type == type }
}