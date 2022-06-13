package com.mthaler.knittings.database.table

import androidx.sqlite.db.SupportSQLiteDatabase

object NeedleTable {
    const val NEEDLES = "needles"

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

    fun create(db: SupportSQLiteDatabase) {
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

    val SQL_ADD_TYPE = "ALTER TABLE " + NEEDLES + " ADD COLUMN " + Cols.TYPE + " TEXT"
}