package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mthaler.dbapp.database.table.*
import com.mthaler.knittings.model.RowCounter

object RowCounterTable {

    val ROWS = "rows"

    object Cols {
        val ID = "_id"
        val TOTAL_ROWS = "total_rows"
        val ROWS_PER_REPEAT = "rows_per_repeat"
        val KNITTING_ID = "knitting_id"
    }

    val Columns = arrayOf(Cols.ID, Cols.TOTAL_ROWS, Cols.ROWS_PER_REPEAT, Cols.KNITTING_ID)

    fun create(db: SQLiteDatabase) {
        val CREATE_ROWS_TABLE = "CREATE TABLE $IF_NOT_EXISTS $ROWS( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.TOTAL_ROWS} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.ROWS_PER_REPEAT} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.KNITTING_ID} $INTEGER $NOT_NULL, " +
                "${FOREIGN_KEY(Cols.KNITTING_ID, KnittingTable.KNITTINGS, KnittingTable.Cols.ID)} )"
        db.execSQL(CREATE_ROWS_TABLE)
    }

    fun cursorToRows(cursor: Cursor): RowCounter {
        val idIndex = cursor.getColumnIndex(Cols.ID)
        val idTotalRows = cursor.getColumnIndex(Cols.TOTAL_ROWS)
        val idRowsPerRepeat = cursor.getColumnIndex(Cols.ROWS_PER_REPEAT)
        val idKnittingIndex = cursor.getColumnIndex(PhotoTable.Cols.KNITTING_ID)

        val id = cursor.getLong(idIndex)
        val totalRows = cursor.getInt(idTotalRows)
        val rowsPerRepeat = cursor.getInt(idRowsPerRepeat)
        val knittingID = cursor.getLong(idKnittingIndex)
        return RowCounter(id, totalRows, rowsPerRepeat, knittingID)
    }

    fun createContentValues(rowCounter: RowCounter, manualID: Boolean = false): ContentValues {
        val values = ContentValues()
        if (manualID) {
            values.put(Cols.ID, rowCounter.id)
        }
        values.put(Cols.TOTAL_ROWS, rowCounter.totalRows)
        values.put(Cols.ROWS_PER_REPEAT, rowCounter.rowsPerRepeat)
        values.put(PhotoTable.Cols.KNITTING_ID, rowCounter.knittingID)
        return values
    }
}