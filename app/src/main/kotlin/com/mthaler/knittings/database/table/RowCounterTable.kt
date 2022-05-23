package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mthaler.knittings.database.table.*
import com.mthaler.knittings.model.RowCounter

object RowCounterTable {

    const val ROW_COUNTERS = "row_counters"

    object Cols {
        val ID = "_id"
        val TOTAL_ROWS = "total_rows"
        val ROWS_PER_REPEAT = "rows_per_repeat"
        val KNITTING_ID = "knitting_id"
    }

    val Columns = arrayOf(Cols.ID, Cols.TOTAL_ROWS, Cols.ROWS_PER_REPEAT, Cols.KNITTING_ID)

    fun create(db: SQLiteDatabase) {
        val CREATE_ROW_COUNTER_TABLE = "CREATE TABLE $IF_NOT_EXISTS $ROW_COUNTERS( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.TOTAL_ROWS} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.ROWS_PER_REPEAT} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.KNITTING_ID} $INTEGER $NOT_NULL, " +
                "${FOREIGN_KEY(Cols.KNITTING_ID, KnittingTable.KNITTINGS, KnittingTable.Cols.ID)} )"
        db.execSQL(CREATE_ROW_COUNTER_TABLE)
    }

    fun create(db: SupportSQLiteDatabase) {
        val CREATE_ROW_COUNTER_TABLE = "CREATE TABLE $IF_NOT_EXISTS $ROW_COUNTERS( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.TOTAL_ROWS} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.ROWS_PER_REPEAT} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.KNITTING_ID} $INTEGER $NOT_NULL, " +
                "${FOREIGN_KEY(Cols.KNITTING_ID, KnittingTable.KNITTINGS, KnittingTable.Cols.ID)} )"
        db.execSQL(CREATE_ROW_COUNTER_TABLE)
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