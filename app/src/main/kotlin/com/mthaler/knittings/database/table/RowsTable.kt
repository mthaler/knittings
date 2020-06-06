package com.mthaler.knittings.database.table

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.model.Photo

object RowsTable {

    val ROWS = "rows"

    object Cols {
        val ID = "_id"
        val TOTAL_ROWS = "total_rows"
        val KNITTING_ID = "knitting_id"
    }

    val Columns = arrayOf(Cols.ID, Cols.TOTAL_ROWS, Cols.KNITTING_ID)

    fun create(db: SQLiteDatabase) {
        val CREATE_ROWS_TABLE = "CREATE TABLE $IF_NOT_EXISTS $ROWS( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.TOTAL_ROWS} $INTEGER $NOT_NULL ${DEFAULT("0")}, " +
                "${Cols.KNITTING_ID} $INTEGER $NOT_NULL, " +
                "${FOREIGN_KEY(Cols.KNITTING_ID, KnittingTable.KNITTINGS, KnittingTable.Cols.ID)} )"
        db.execSQL(CREATE_ROWS_TABLE)
    }

    fun cursorToRows(cursor: Cursor): Photo {
        val idIndex = cursor.getColumnIndex(Cols.ID)
        val idTotalRows = cursor.getColumnIndex(Cols.TOTAL_ROWS)
        val idKnittingIndex = cursor.getColumnIndex(PhotoTable.Cols.KNITTING_ID)

        val id = cursor.getLong(idIndex)
        val totalRows = cursor.getInt(idTotalRows)
        val knittingID = cursor.getLong(idKnittingIndex)
        return null!!
    }
}