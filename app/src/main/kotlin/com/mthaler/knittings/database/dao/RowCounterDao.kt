package com.mthaler.knittings.database.dao

import androidx.room.*
import com.mthaler.knittings.model.RowCounter

@Dao
interface RowCounterDao {

    @Insert
    fun insertAll(vararg rowCounter: RowCounter)

    @Delete
    fun delete(rowCounter: RowCounter)

    @Query("SELECT * FROM row_counters")
    fun getAll(): List<RowCounter>

    @Query("SELECT * FROM row_counters WHERE id=:id")
    fun get(id: Long): RowCounter
}