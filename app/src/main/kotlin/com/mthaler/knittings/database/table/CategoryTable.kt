package com.mthaler.knittings.database.table

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mthaler.knittings.model.Category
import com.mthaler.knittings.database.table.SqlHelper.INTEGER
import com.mthaler.knittings.database.table.SqlHelper.TEXT
import com.mthaler.knittings.database.table.SqlHelper.PRIMARY_KEY
import com.mthaler.knittings.database.table.SqlHelper.AUTOINCREMENT
import com.mthaler.knittings.database.table.SqlHelper.NOT_NULL
import com.mthaler.knittings.database.table.SqlHelper.IF_NOT_EXISTS

object CategoryTable {
    val CATEGORY = "category"

    val Columns = arrayOf(Cols.ID, Cols.NAME, Cols.COLOR)

    object Cols {
        val ID = "_id"
        val NAME = "name"
        val COLOR = "color"
    }

    fun create(db: SQLiteDatabase) {
        val CREATE_CATEGORY_TABLE = "CREATE TABLE $IF_NOT_EXISTS $CATEGORY ( " +
                "${Cols.ID} $INTEGER $PRIMARY_KEY $AUTOINCREMENT, " +
                "${Cols.NAME} $TEXT $NOT_NULL," +
                "${Cols.COLOR} $INTEGER )"
        db.execSQL(CREATE_CATEGORY_TABLE)
    }

    fun cursorToCategory(cursor: Cursor): Category {
        val idIndex = cursor.getColumnIndex(Cols.ID)
        val idName = cursor.getColumnIndex(Cols.NAME)
        val idColor = cursor.getColumnIndex(Cols.COLOR)

        val id = cursor.getLong(idIndex)
        val name = cursor.getString(idName)
        val color = if (cursor.isNull(idColor)) null else cursor.getInt(idColor)
        return Category(id, name, color)
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