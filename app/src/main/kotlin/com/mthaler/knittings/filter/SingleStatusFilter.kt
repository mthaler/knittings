package com.mthaler.knittings.filter

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status

/**
 * A filter that filters the project list by status
 *
 * @param status status used for filtering
 */
data class SingleStatusFilter(val status: Status) : Filter<Knitting> {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.status == status }
}
