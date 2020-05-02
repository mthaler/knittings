package com.mthaler.knittings.compressphotos

data class Statistics(val photos: Int, val totalSize: Long, val photosToCompress: Int) {

    companion object {

        val EMPTY = Statistics(0, 0, 0)
    }
}