package com.mthaler.knittings.model

import java.io.Serializable

data class Category(
    val id: Long = -1,
    val name: String = "",
    val color: Int? = null) : Serializable {

    companion object {

        val EMPTY = Category()
    }
}