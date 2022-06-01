package com.mthaler.knittings.database.dao

import androidx.room.*
import com.mthaler.knittings.model.Needle

@Dao
interface NeedleDao {

    @Insert
    fun insertAll(vararg needles: Needle)

    @Delete
    fun delete(needle: Needle)

    @Query("SELECT * FROM needles")
    fun getAll(): List<Needle>

    @Query("SELECT * FROM needles WHERE id=:id")
    fun get(id: Long): Needle
}