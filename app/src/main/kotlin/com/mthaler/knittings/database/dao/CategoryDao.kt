package com.mthaler.knittings.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mthaler.knittings.model.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(knitting: Category): Long

    @Delete
    fun delete(category: Category)

    @Query("SELECT * FROM CATEGORY")
    fun getAll(): List<Category>

    @Query("SELECT * FROM CATEGORY WHERE id=:id")
    fun get(id: Long): LiveData<Category>
}