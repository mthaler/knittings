package com.mthaler.knittings.database

import com.mthaler.dbapp.database.*
import com.mthaler.knittings.database.table.RowCounterTable
import com.mthaler.knittings.model.RowCounter

object RowCounterConverter {

    fun convert(dbRow: Map<String, Any?>): RowCounter {
        val id = dbRow.getLong(RowCounterTable.Cols.ID)
        val totalRows = dbRow.getInt(RowCounterTable.Cols.TOTAL_ROWS)
        val rowsPerRepeat = dbRow.getInt(RowCounterTable.Cols.ROWS_PER_REPEAT)
        val knittingID = dbRow.getLong(RowCounterTable.Cols.KNITTING_ID)
        return RowCounter(id, totalRows, rowsPerRepeat, knittingID)
    }
}