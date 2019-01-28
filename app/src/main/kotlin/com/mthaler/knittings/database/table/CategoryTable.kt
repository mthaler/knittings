package com.mthaler.knittings.database.table

import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

object CategoryTable {
    val CATEGORY = "category"

    val Columns = arrayOf(Cols.ID, Cols.NAME, Cols.COLOR)

    object Cols {
        val ID = "_id"
        val NAME = "name"
        val COLOR = "color"
    }

    fun create(db: SQLiteDatabase) {
        db.createTable(CATEGORY, true,
                Cols.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                Cols.NAME to TEXT + NOT_NULL,
                Cols.COLOR to INTEGER)
    }
}