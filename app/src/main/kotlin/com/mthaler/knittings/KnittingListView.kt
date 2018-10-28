package com.mthaler.knittings

interface KnittingListView {

    fun addKnitting()

    fun getSorting(): Sorting

    fun setSorting(sorting: Sorting)

    fun getFilter(): Filter

    fun setFilter(filter: Filter)

    fun updateKnittingList()
}
