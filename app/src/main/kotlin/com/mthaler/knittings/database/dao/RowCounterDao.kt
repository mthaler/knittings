package com.mthaler.knittings.database.dao

import androidx.room.*
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.RowCounter

@Dao
interface RowCounterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rowCounter: RowCounter): Long

    @Delete
    fun delete(rowCounter: RowCounter)

    @Query("SELECT * FROM row_counters")
    fun getAll(): List<RowCounter>

    @Query("SELECT * FROM row_counters WHERE knitting_id=knittingID")
    fun loadAllUsersBetweenAges(knitting: Knitting): Array<RowCounter>


    @Query("SELECT * FROM row_counters WHERE id=:id")
    fun get(id: Long): RowCounter
}