package com.mthaler.knittings

interface KnittingListView {

    fun addKnitting()

    /**
     * Get the current sorting used for the knitting list
     */
    fun getSorting(): Sorting

    /**
     * Set the sorting of the knitting list. updateKnittingList needs to be called after setting the sorting.
     *
     * @param sorting sorting
     */
    fun setSorting(sorting: Sorting)

    /**
     * Get the current filter used for the filtering the knitting list
     */
    fun getFilter(): Filter

    /**
     * Set the filter used for filtering the knitting list. updateKnittingList needs to be called after setting the sorting
     *
     * @param filter filter
     */
    fun setFilter(filter: Filter)

    /**
     * Updates the knitting list. This needs to be called if the sorting or the filter is changed
     */
    fun updateKnittingList()
}
