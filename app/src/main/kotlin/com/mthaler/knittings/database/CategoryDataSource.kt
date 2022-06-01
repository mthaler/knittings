package com.mthaler.knittings.database

import com.mthaler.knittings.model.Category

interface CategoryDataSource {

    val allCategories: List<Category>

    fun getCategory(id: Long): Category?

    fun addCategory(category: Category): Category

    fun updateCategory(category: Category): Category

    fun deleteCategory(category: Category)

    fun deleteAllCategories()
}