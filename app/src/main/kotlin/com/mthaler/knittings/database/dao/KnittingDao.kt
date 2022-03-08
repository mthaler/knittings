package com.mthaler.knittings.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mthaler.knittings.model.Knitting

@Dao
interface KnittingDao {
    @Insert
    fun insertAll(vararg knittings: Knitting)

    @Delete
    fun delete(knitting: Knitting)

    @Query("SELECT * FROM KNITTINGS")
    fun getAll(): List<Knitting>
}