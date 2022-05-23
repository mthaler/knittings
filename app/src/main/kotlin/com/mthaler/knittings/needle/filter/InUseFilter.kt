package com.mthaler.knittings.needle.filter

import com.mthaler.knittings.filter.Filter
import com.mthaler.knittings.model.Needle

data class InUseFilter(val inUse: Boolean) : Filter<Needle> {

    override fun filter(items: List<Needle>): List<Needle> = items.filter { it.inUse == inUse }
}