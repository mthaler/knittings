package com.mthaler.knittings.database.dao

import androidx.room.*
import com.mthaler.knittings.model.Photo

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photo: Photo): Long

    @Delete
    fun delete(photo: Photo)

    @Query("SELECT * FROM photos")
    fun getAll(): List<Photo>

    @Query("SELECT * FROM photos WHERE id=:id")
    fun get(id: Long): Photo
}