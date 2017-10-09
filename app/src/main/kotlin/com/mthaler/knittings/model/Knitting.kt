package com.mthaler.knittings.model

import java.text.SimpleDateFormat
import java.util.Date

/**
 * The Knitting class stores data for a single knitting
 */
data class Knitting(val id: Long,
                    val title: String = "",
                    val description: String = "",
                    val started: Date = Date(),
                    val finished: Date? = null,
                    val needleDiameter: Double = 0.0,
                    val size: Double = 0.0,
                    val defaultPhoto: Photo? = null,
                    val rating: Double = 0.0) {

    val photoFilename: String
        get() {
            val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date())
            return "IMG_$timeStamp.jpg"
        }
}
