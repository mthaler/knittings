package com.mthaler.knittings.database

import com.mthaler.knittings.database.table.CategoryTable
import com.mthaler.knittings.model.Category

object CategoryConverter {

    fun convert(dbRow: Map<String, Any?>): Category {
        val id = dbRow.getLong(CategoryTable.Cols.ID)
        val name = dbRow.getString(CategoryTable.Cols.NAME)
        val color =  if (dbRow.containsKey(CategoryTable.Cols.COLOR) && dbRow.get(CategoryTable.Cols.COLOR) != null) dbRow.getInt(
            CategoryTable.Cols.COLOR)  else null
        return Category(id, name, color)
    }
}