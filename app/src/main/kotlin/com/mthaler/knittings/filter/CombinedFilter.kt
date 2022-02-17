package com.mthaler.knittings.filter

data class CombinedFilter<T>(val filters: List<Filter<T>>) : Filter<T> {

    override fun filter(items: List<T>): List<T> {
        var result = items
        for (filter in filters) {
            result = filter.filter(result)
        }
        return result
    }

    companion object {

        // empty list that does no filtering
        fun <T>empty() = CombinedFilter<T>(emptyList())
    }
}