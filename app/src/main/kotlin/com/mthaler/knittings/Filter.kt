package com.mthaler.knittings

import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Knitting

interface Filter {

    fun filter(knittings: List<Knitting>): List<Knitting>
}

object NoFilter : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings
}

data class SingleCategoryFilter(val category: Category) : Filter {

    override fun filter(knittings: List<Knitting>): List<Knitting> = knittings.filter { it.category == category }
}
