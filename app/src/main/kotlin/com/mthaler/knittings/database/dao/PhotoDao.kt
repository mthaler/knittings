package com.mthaler.knittings.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.Photo

@Dao
interface PhotoDao {
    @Insert
    fun insertAll(vararg photos: Photo)

    @Delete
    fun delete(photo: Photo)

    @Query("SELECT * FROM photos")
    fun getAll(): List<Photo>

    @Query("SELECT * FROM photos WHERE id=:id")
    fun get(id: Long): Needle
}