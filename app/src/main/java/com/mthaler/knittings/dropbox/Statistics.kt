package com.mthaler.knittings.dropbox

data class Statistics(val photos: Int, val totalSize: Long) {

    companion object {

        val EMPTY = Statistics(0, 0)
    }
}