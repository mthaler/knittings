package com.mthaler.knittings.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

/**
 * The Knitting class stores data for a single knitting
 */
@Entity
data class Knitting(
    @PrimaryKey val id: Long = -1,
    val title: String = "",
    val description: String = "",
    val started: Date = Date(),
    val finished: Date? = null,
    val needleDiameter: String = "",
    val size: String = "",
    val defaultPhoto: Photo? = null,
    val rating: Double = 0.0,
    val duration: Long = 0L,
    val category: Category? = null,
    val status: Status = Status.PLANNED
) : Serializable {

    companion object {

        val EMPTY = Knitting()
    }
}
