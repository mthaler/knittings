package com.mthaler.knittings.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Category(
    @PrimaryKey val id: Long = -1,
    val name: String = "",
    val color: Int? = null) : Serializable {

    companion object {

        val EMPTY = Category()
    }
}