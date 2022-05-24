package com.mthaler.knittings.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mthaler.knittings.model.Knitting

@Dao
interface KnittingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(knitting: Knitting): Long

    @Delete
    fun delete(knitting: Knitting)

    @Query("SELECT * FROM KNITTINGS")
    fun getAll(): List<Knitting>

    @Query("SELECT * FROM KNITTINGS WHERE id=:id")
    fun get(id: Long): LiveData<Knitting>
}