package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.database.table.*
import com.mthaler.knittings.model.Needle

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
        val CREATE_NEEDLE_TABLE = "CREATE TABLE $IF_NOT_EXISTS $NEEDLES ( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.NAME} $TEXT $NOT_NULL, " +
                "${Cols.DESCRIPTION} $TEXT $NOT_NULL, " +
                "${Cols.SIZE} $TEXT $NOT_NULL, " +
                "${Cols.LENGTH} $TEXT $NOT_NULL, " +
                "${Cols.MATERIAL} $TEXT $NOT_NULL, " +
                "${Cols.IN_USE} $INTEGER, " +
                "${Cols.TYPE} $TEXT $NOT_NULL )"
        db.execSQL(CREATE_NEEDLE_TABLE)
    }

    /**
     * Creates the content values map used to insert a needle into the database or update an existing needle
     *
     * @param needle needle
     * @param manualID should we manually insert id?
     * @return content values for inserting or updating needle
     */
    fun createContentValues(needle: Needle, manualID: Boolean = false): ContentValues {
        val values = ContentValues()
        if (manualID) {
            values.put(Cols.ID, needle.id)
        }
        values.put(Cols.NAME, needle.name)
        values.put(Cols.DESCRIPTION, needle.description)
        values.put(Cols.SIZE, needle.size)
        values.put(Cols.LENGTH, needle.length)
        values.put(Cols.MATERIAL, needle.material.name)
        values.put(Cols.IN_USE, needle.inUse)
        values.put(Cols.TYPE, needle.type.name)
        return values
    }

    val SQL_ADD_TYPE = "ALTER TABLE " + NEEDLES + " ADD COLUMN " + Cols.TYPE + " TEXT"
}