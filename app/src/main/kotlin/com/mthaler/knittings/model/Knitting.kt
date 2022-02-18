package com.mthaler.knittings.model

import java.io.Serializable
import java.util.Date

/**
 * The Knitting class stores data for a single knitting
 */
data class Knitting(
        override val id: Long = -1,
        override val title: String = "",
        override val description: String = "",
        override val started: Date = Date(),
        override val finished: Date? = null,
        val needleDiameter: String = "",
        val size: String = "",
        override val defaultPhoto: Photo? = null,
        val rating: Double = 0.0,
        val duration: Long = 0L,
        override val category: Category? = null,
        val status: Status = Status.PLANNED
) : Serializable, Project {

    companion object {

        val EMPTY = Knitting()
    }
}
