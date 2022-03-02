package com.mthaler.knittings.database.dao

import androidx.room.*
import com.mthaler.knittings.model.Category

@Dao
interface CategoryDao {

    @Insert
    fun insertAll(vararg categorieds: Category)

    @Delete
    fun delete(category: Category)

    @Query("SELECT * FROM CATEGORY")
    fun getAll(): List<Category>
}