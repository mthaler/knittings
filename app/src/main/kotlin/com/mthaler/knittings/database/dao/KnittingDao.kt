package com.mthaler.knittings.database.dao

import androidx.room.*
import com.mthaler.knittings.model.Knitting

@Dao
interface KnittingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(knitting: Knitting): Long

    @Delete
    fun delete(knitting: Knitting)

    @Query("SELECT * FROM knittings")
    fun getAll(): List<Knitting>

    @Query("SELECT * FROM knittings WHERE id=:id")
    fun get(id: Long): Knitting
}