package com.mthaler.knittings.database.typeconveters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.mthaler.knittings.model.Photo

class BitmapConverters {

     @TypeConverter
     public fun byteArrayToBitmap(value: ByteArray?): Bitmap? {
         if (value != null) {
             val options = BitmapFactory.Options()
             val preview = BitmapFactory.decodeByteArray(value, 0, value.size, options)
             return preview
         } else {
             return null
         }
     }

    @TypeConverter
    public fun previewToByteArray(b: Bitmap?) = Photo.getBytes(b)
}