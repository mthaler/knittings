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

    fun deleteCategory() {
        val category = datasource.getCategory(categoryID)
        datasource.deleteCategory(category)
    }

    fun saveCategory(category: Category) {
        if (category.id == -1L) {
            datasource.addCategory(category)
        } else {
            datasource.updateCategory(category)
        }
    }

    override fun databaseChanged() {
        val c = datasource.getCategory(categoryID)
        category.setMutVal(c)
    }
}