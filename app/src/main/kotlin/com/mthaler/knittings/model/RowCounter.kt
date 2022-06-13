package com.mthaler.knittings.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mthaler.knittings.database.table.RowCounterTable
import java.io.Serializable

@Entity(tableName = RowCounterTable.ROW_COUNTERS)
data class RowCounter(
    @PrimaryKey val id: Long = -1,
    @ColumnInfo(name = "total_rows") val totalRows: Int = 0,
    @ColumnInfo(name = "rows_per_repeat") val rowsPerRepeat: Int = 0,
    @ColumnInfo(name = "knitting_id") val knittingID: Long = -1) : Serializable {

    companion object {

        val EMPTY = RowCounter()
    }
}