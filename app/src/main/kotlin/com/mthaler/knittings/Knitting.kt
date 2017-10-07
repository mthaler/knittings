package com.mthaler.knittings

import java.text.SimpleDateFormat
import java.util.Date

/**
 * The Knitting class stores data for a single knitting
 */
class Knitting(val id: Long) {
    var title: String? = null
    var description: String? = null
    var started: Date? = null
    var finished: Date? = null
    var needleDiameter: Double = 0.toDouble()
    var size: Double = 0.toDouble()
    var defaultPhoto: Photo? = null
    var rating: Double = 0.toDouble()

    val photoFilename: String
        get() {
            val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date())
            return "IMG_$timeStamp.jpg"
        }

    init {
        title = ""
        description = ""
        started = Date()
        finished = null
        needleDiameter = 0.0
        size = 0.0
        rating = 0.0
    }

    override fun toString(): String {
        return "Knitting{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", started=" + started +
                ", finished=" + finished +
                ", needleDiameter=" + needleDiameter +
                ", size=" + size +
                ", defaultPhoto=" + defaultPhoto +
                ", rating=" + rating +
                '}'
    }
}
