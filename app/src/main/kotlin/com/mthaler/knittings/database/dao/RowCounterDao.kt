package com.mthaler.knittings.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mthaler.knittings.model.RowCounter

@Dao
interface RowCounterDao {

    @Insert
    fun insertAll(vararg rowCounter: RowCounter)

    @Delete
    fun delete(rowCounter: RowCounter)

    @Query("SELECT * FROM row_counters")
    fun getAll(): List<RowCounter>
}