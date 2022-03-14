package com.mthaler.knittings.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class RowCounter(
    @PrimaryKey val id: Long = -1,
    val totalRows: Int = 0,
    val rowsPerRepeat: Int = 0,
    val knittingID: Long = -1) : Serializable {

    companion object {

        val EMPTY = RowCounter()
    }
}