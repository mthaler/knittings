package com.mthaler.knittings.model

<<<<<<< HEAD
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mthaler.knittings.database.table.CategoryTable
import java.io.Serializable

@Entity(tableName = CategoryTable.CATEGORY)
=======
import java.io.Serializable

>>>>>>> master
data class Category(
    val id: Long = -1,
    val name: String = "",
    val color: Int? = null) : Serializable {

    companion object {

        val EMPTY = Category()
    }
}