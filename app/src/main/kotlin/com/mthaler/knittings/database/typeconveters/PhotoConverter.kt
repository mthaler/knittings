package com.mthaler.knittings.database.typeconveters

import androidx.room.TypeConverter
import com.mthaler.knittings.model.Photo
import java.util.*

class PhotoConverter {

    @TypeConverter
    public fun idToPhoto(value: Long?): Date? {
        if (value != null) {
            return
        } else {
            return null
        }
    }

     @TypeConverter
     public fun photoToID(p: Photo?): Long {
        if (p != null) {
            return p.id
        } else {
            return -1L
        }
     }
}