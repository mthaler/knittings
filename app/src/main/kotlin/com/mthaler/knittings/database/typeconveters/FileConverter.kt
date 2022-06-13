package com.mthaler.knittings.database.typeconveters

import androidx.room.TypeConverter
import java.io.File

class FileConverter {

     @TypeConverter
     public fun stringToFile(value: String?): File? {
         if (value != null) {
             return File(value)
         } else {
             return null
         }
     }

    @TypeConverter
    public fun fileToSting(f: File?): String? {
        if (f != null) {
            return f.absolutePath
        } else {
            return null
        }
    }
}