package com.mthaler.knittings.model

import com.mthaler.knittings.database.getInt
import com.mthaler.knittings.database.getLong
import com.mthaler.knittings.database.getString
import com.mthaler.knittings.database.table.CategoryTable
import java.lang.IllegalArgumentException

abstract class AbstractExportDatabase : ExportDatabase {

    override fun checkValidity() {
        checkPhotosValidity()
        checkProjectsValidity()
    }

    private fun checkPhotosValidity() {
        val missing =  photos.map {it.ownerID}.toSet() - projects.map { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException("Photos reference non-existing knittings with ids $missing")
        }
    }

    private fun checkProjectsValidity() {
        val missingCategories = projects.mapNotNull { it.category }.map { it.id }.toSet() - categories.map { it.id }.toSet()
        if (missingCategories.isNotEmpty()) {
            throw IllegalArgumentException("Knittings reference non-existing categories with ids $missingCategories")
        }
        val missingPhotos = projects.mapNotNull { it.defaultPhoto }.map { it.id }.toSet() - photos.map { it.id }.toSet()
        if (missingPhotos.isNotEmpty()) {
            throw IllegalArgumentException("Knittings reference non-existing photos with ids $missingPhotos")
        }
    }

    fun convert(dbRow: Map<String, Any?>): Category {
        val id = dbRow.getLong(CategoryTable.Cols.ID)
        val name = dbRow.getString(CategoryTable.Cols.NAME)
        val color =  if (dbRow.containsKey(CategoryTable.Cols.COLOR) && dbRow.get(CategoryTable.Cols.COLOR) != null) dbRow.getInt(
            CategoryTable.Cols.COLOR)  else null
        return Category(id, name, color)
    }
}