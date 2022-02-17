package com.mthaler.knittings.model

import java.io.Serializable

data class RowCounter(val id: Long = -1, val totalRows: Int = 0, val rowsPerRepeat: Int = 0, val knittingID: Long = -1) : Serializable {

    companion object {

        val EMPTY = RowCounter()
    }
}