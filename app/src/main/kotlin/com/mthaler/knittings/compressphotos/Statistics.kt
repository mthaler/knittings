package com.mthaler.knittings.compressphotos

data class Statistics(val photos: Int, val totalSize: Long) {

    companion object {

        val EMPTY = Statistics(0, 0)
    }
}