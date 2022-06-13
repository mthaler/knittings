package com.mthaler.knittings.database.table

import androidx.sqlite.db.SupportSQLiteDatabase

object CategoryTable {
    const val CATEGORY = "category"

    val Columns = arrayOf(Cols.ID, Cols.NAME, Cols.COLOR)

    object Cols {
        val ID = "_id"
        val NAME = "name"
        val COLOR = "color"
    }

    fun create(db: SupportSQLiteDatabase) {
        val createTable = "CREATE TABLE $IF_NOT_EXISTS $CATEGORY ( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.NAME} $TEXT $NOT_NULL, " +
                "${Cols.COLOR} $INTEGER )"
        db.execSQL(createTable)
    }
}