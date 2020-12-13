package com.mthaler.knittings.needle

import com.mthaler.dbapp.filter.Filter
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleType

data class SingleTypeFilter(val type: NeedleType) : Filter<Needle> {

    override fun filter(needles: List<Needle>): List<Needle> = needles.filter { it.type == type }
}