package com.mthaler.knittings.category

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Category
import com.mthaler.knittings.utils.setMutVal

class EditCategoryViewModel(application: Application) : DatasourceViewModel(application) {

    private var categoryID = -1L
    val category = MutableLiveData<Category>()

    fun init(id: Long) {
        if (id != categoryID) {
            categoryID = id
            val c = datasource.getCategory(id)
            category.setMutVal(c)
        }
    }

    fun updateCategoryŃame(name: String) {
        val category = datasource.getCategory(categoryID)
        if (name != category.name) {
            val updatedCategory = category.copy(name = name)
            datasource.updateCategory(updatedCategory)
        }
    }

    fun updateCategoryColor(color: Int) {
        val category = datasource.getCategory(categoryID)
        if (color != category.color) {
            val updatedCategory = category.copy(color = color)
            datasource.updateCategory(updatedCategory)
        }
    }

    fun deleteCategory() {
        val category = datasource.getCategory(categoryID)
        datasource.deleteCategory(category)
    }

    fun getCategoryName(): String {
        val category = datasource.getCategory(categoryID)
        return category.name
    }

    override fun databaseChanged() {
        val c = datasource.getCategory(categoryID)
        category.setMutVal(c)
    }
}