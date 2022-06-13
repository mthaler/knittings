package com.mthaler.knittings.database

import androidx.room.TypeConverter
import java.util.*

class DateConverter {

     @TypeConverter
     public fun millisToDate(value: Long?): Date? {
         if (value != null) {
             return Date(value)
         } else {
             return null
         }
     }

     @TypeConverter
     public fun dateToMillis(d: Date?): Long {
        if (d != null) {
            return d.time
        } else {
            return -1L
        }
     }
}