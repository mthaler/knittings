package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mthaler.knittings.model.Category

object CategoryTable {
    const val CATEGORY = "category"

    val Columns = arrayOf(Cols.ID, Cols.NAME, Cols.COLOR)

    object Cols {
        val ID = "_id"
        val NAME = "name"
        val COLOR = "color"
    }

    fun create(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $IF_NOT_EXISTS $CATEGORY ( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.NAME} $TEXT $NOT_NULL, " +
                "${Cols.COLOR} $INTEGER )"
        db.execSQL(createTable)
    }

    fun create(db: SupportSQLiteDatabase) {
        val createTable = "CREATE TABLE $IF_NOT_EXISTS $CATEGORY ( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.NAME} $TEXT $NOT_NULL, " +
                "${Cols.COLOR} $INTEGER )"
        db.execSQL(createTable)
    }

    /**
     * Creates the content values map used to insert a category into the database or update an existing category
     *
     * @param category category
     * @param manualID should we manually insert id?
     * @return content values for inserting or updating category
     */
    fun createContentValues(category: Category, manualID: Boolean = false): ContentValues {
        val values = ContentValues()
        if (manualID) {
            values.put(Cols.ID, category.id)
        }
        values.put(Cols.NAME, category.name)
        if (category.color != null) {
            values.put(Cols.COLOR, category.color)
        } else {
            values.putNull(Cols.COLOR)
        }
        return values
    }
}