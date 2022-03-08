package com.mthaler.knittings.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mthaler.knittings.model.Knitting

interface KnittingDao {
    @Insert
    fun insertAll(vararg knittings: Knitting)

    @Delete
    fun delete(knitting: Knitting)

    @Query("SELECT * FROM KNITTINGS")
    fun getAll(): List<Knitting>
}