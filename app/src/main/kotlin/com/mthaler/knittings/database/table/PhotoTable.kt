package com.mthaler.knittings.database.table

import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

object PhotoTable {
    val PHOTOS = "photos"

    val Columns = arrayOf(Cols.ID, Cols.FILENAME, Cols.PREVIEW, Cols.DESCRIPTION, Cols.KNITTING_ID)

    fun create(db: SQLiteDatabase) {
        db.createTable(PHOTOS, true,
                Cols.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                Cols.FILENAME to TEXT + NOT_NULL,
                Cols.PREVIEW to BLOB,
                Cols.DESCRIPTION to TEXT + NOT_NULL,
                Cols.KNITTING_ID to INTEGER + NOT_NULL,
                FOREIGN_KEY(Cols.KNITTING_ID, KnittingTable.KNITTINGS, KnittingTable.Cols.ID))
    }

    object Cols {
        val ID = "_id"
        val FILENAME = "filename"
        val PREVIEW = "preview"
        val DESCRIPTION = "description"
        val KNITTING_ID = "knitting_id"
    }
}