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

    @Query("DELETE FROM row_counters WHERE row_counters.knitting_id=:knittingID")
    fun delete(knittingID: Long)

    @Query("SELECT * FROM row_counters")
    fun getAll(): List<RowCounter>

    @Query("SELECT * FROM row_counters WHERE row_counters.knitting_id=:knittingID")
    fun getAll(knittingID: Long): Array<RowCounter>


}