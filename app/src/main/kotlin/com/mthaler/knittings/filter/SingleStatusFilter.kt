package com.mthaler.knittings.filter

import android.widget.RemoteViews
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status

/**
 * A filter that filters the project list by status
 *
 * @param status status used for filtering
 */
data class SingleStatusFilter(val status: Status) : Filter<Knitting> {

    override fun filter(items: List<Knitting>): List<Knitting> = items.filter { it.status == status }
}
