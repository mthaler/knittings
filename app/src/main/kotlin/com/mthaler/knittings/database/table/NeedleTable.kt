package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import org.jetbrains.anko.db.*
import java.lang.Exception

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

    fun cursorToNeedle(context: Context, cursor: Cursor): Needle {
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
        val materialStr = cursor.getString(idMaterial)
        val material = try {
            NeedleMaterial.valueOf(materialStr)
        } catch (ex: Exception) {
            NeedleMaterial.parse(context, materialStr)
        }
        val inUse = cursor.getInt(idInUse)
        val typeStr = cursor.getString(idType)
        val type = try {
            NeedleType.valueOf(typeStr)
        } catch (ex: Exception) {
            NeedleType.parse(context, typeStr)
        }
        return Needle(id, name, description, size, length, material, inUse > 0, type)
    }

    /**
     * Creates the content values map used to insert a needle into the database or update an existing needle
     */
    fun createContentValues(needle: Needle, manualID: Boolean = false): ContentValues {
        val values = ContentValues()
        if (manualID) {
            values.put(NeedleTable.Cols.ID, needle.id)
        }
        values.put(NeedleTable.Cols.NAME, needle.name)
        values.put(NeedleTable.Cols.DESCRIPTION, needle.description)
        values.put(NeedleTable.Cols.SIZE, needle.size)
        values.put(NeedleTable.Cols.LENGTH, needle.length)
        values.put(NeedleTable.Cols.MATERIAL, needle.material.name)
        values.put(NeedleTable.Cols.IN_USE, needle.inUse)
        values.put(NeedleTable.Cols.TYPE, needle.type.name)
        return values
    }

    val SQL_ADD_TYPE = "ALTER TABLE " + NEEDLES + " ADD COLUMN " + Cols.TYPE + " TEXT"
}