package com.mthaler.knittings.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mthaler.knittings.model.Needle

interface NeedleDao {

    @Insert
    fun insertAll(vararg needles: Needle)

    @Delete
    fun delete(needle: Needle)

    @Query("SELECT * FROM NEEDLES")
    fun getAll(): List<Needle>
}