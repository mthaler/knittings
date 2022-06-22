package com.mthaler.knittings.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mthaler.knittings.database.table.CategoryTable
import java.io.Serializable

@Entity(tableName = CategoryTable.CATEGORY)
data class Category(
    @PrimaryKey val id: Long = -1,
    val name: String = "",
    val color: Int? = null) : Serializable {

    companion object {

        val EMPTY = Category()
    }
}