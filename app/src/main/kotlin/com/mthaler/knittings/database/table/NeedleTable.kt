package com.mthaler.knittings.database.table

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.model.Needle
import org.jetbrains.anko.db.*

object NeedleTable {
    val NEEDLES = "needles"

    val Columns = arrayOf(Cols.ID, Cols.NAME, Cols.DESCRIPTION, Cols.SIZE, Cols.LENGTH, Cols.MATERIAL, Cols.IN_USE, Cols.TYPE)

    object Cols {
        val ID = "_id"
        val NAME = "name"
        val DESCRIPTION = "description"
        val SIZE = "size"
        val LENGTH = "length"
        val MATERIAL = "material"
        val IN_USE = "in_use"
        val TYPE = "type"
    }

    fun create(db: SQLiteDatabase) {
        db.createTable(NEEDLES, true,
                Cols.ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                Cols.NAME to TEXT + NOT_NULL,
                Cols.DESCRIPTION to TEXT + NOT_NULL,
                Cols.SIZE to TEXT + NOT_NULL,
                Cols.LENGTH to TEXT + NOT_NULL,
                Cols.MATERIAL to TEXT + NOT_NULL,
                Cols.IN_USE to INTEGER,
                Cols.TYPE to TEXT + NOT_NULL)
    }

    fun cursorToNeedle(cursor: Cursor): Needle {
        val idIndex = cursor.getColumnIndex(Cols.ID)
        val idName = cursor.getColumnIndex(Cols.NAME)
        val idDescription = cursor.getColumnIndex(Cols.DESCRIPTION)
        val idSize = cursor.getColumnIndex(Cols.SIZE)
        val idLength = cursor.getColumnIndex(Cols.LENGTH)
        val idMaterial = cursor.getColumnIndex(Cols.MATERIAL)
        val idInUse = cursor.getColumnIndex(Cols.IN_USE)
        val idType = cursor.getColumnIndex(Cols.TYPE)

        val id = cursor.getLong(idIndex)
        val name = cursor.getString(idName)
        val description = cursor.getString(idDescription)
        val size = cursor.getString(idSize)
        val length = cursor.getString(idLength)
        val material = cursor.getString(idMaterial)
        val inUse = cursor.getInt(idInUse)
        val type = cursor.getString(idType)
        return Needle(id, name, description, size, length, material, inUse > 0, type)
    }

    val SQL_ADD_STATUS = "ALTER TABLE " + NEEDLES + " ADD COLUMN " + Cols.TYPE + " TEXT"
}