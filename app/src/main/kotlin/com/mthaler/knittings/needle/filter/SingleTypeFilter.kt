package com.mthaler.knittings.needle.filter

import com.mthaler.knittings.filter.Filter
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleType

data class SingleTypeFilter(val type: NeedleType) : Filter<Needle> {

    override fun filter(items: List<Needle>): List<Needle> = items.filter { it.type == type }
}