package com.mthaler.knittings.needle

import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleType
import java.io.Serializable

interface Filter : Serializable {

    fun filter(needles: List<Needle>): List<Needle>
}

object NoFilter : Filter {

    override fun filter(needles: List<Needle>): List<Needle> = needles
}

data class SingleTypeFilter(val type: NeedleType) : Filter {

    override fun filter(needles: List<Needle>): List<Needle> = needles.filter { it.type == type }
}