package com.mthaler.knittings.database.table

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.model.Needle
import org.jetbrains.anko.db.*

object NeedleTable {
    val NEEDLES = "needles"

    val Columns = arrayOf(Cols.ID, Cols.NAME)

    object Cols {
        val ID = "_id"
        val NAME = "name"
    }

    fun create(db: SQLiteDatabase) {
        db.createTable(NEEDLES, true,
                Cols.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                Cols.NAME to TEXT + NOT_NULL)
    }

    fun cursorToNeedle(cursor: Cursor): Needle {
        val idIndex = cursor.getColumnIndex(Cols.ID)
        val idName = cursor.getColumnIndex(Cols.NAME)

        val id = cursor.getLong(idIndex)
        val name = cursor.getString(idName)
        return Needle(id, name)
    }
}