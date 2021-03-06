package com.mthaler.knittings.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import java.io.*
import java.util.Arrays

fun Uri.getOrientation(context: Context): Int {
    var inputStream: InputStream? = null
    try {
        inputStream = context.contentResolver.openInputStream(this)
        val exif = ExifInterface(inputStream!!)
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
    } catch (ex: IOException) {
        ex.printStackTrace()
        return ExifInterface.ORIENTATION_UNDEFINED
    } finally {
        inputStream?.close()
    }
}

object PictureUtils {

    private const val TAG = "PictureUtils"

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromPath(path: String, reqWidth: Int, reqHeight: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    /**
     * Returns a new rotated copy of the bitmap and recycles the old one
     * @param bitmap bitmap to be rotated
     * @param orientation orientation
     * @return rotated bitmap
     */
    fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            rotated
        } catch (ex: OutOfMemoryError) {
            ex.printStackTrace()
            bitmap
        }
    }

    fun copy(src: Uri, dst: File, context: Context) {
        val chunkSize = 1024 // We'll read in one kB at a time
        val imageData = ByteArray(chunkSize)
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = context.contentResolver.openInputStream(src)!!
            out = FileOutputStream(dst) // I'm assuming you already have the File object for where you're writing to

            var bytesRead: Int = `in`.read(imageData)
            while (bytesRead > 0) {
                out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)))
                bytesRead = `in`.read(imageData)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Could not copy data", ex)
        } finally {
            `in`?.close()
            out?.close()
        }
    }

    suspend fun compress(context: Context, imageFile: File): File {
        return Compressor.compress(context, imageFile) {
            resolution(1024, 1024)
            quality(80)
            format(Bitmap.CompressFormat.JPEG)
            size(350 * 1024) // 350 KB
        }
    }
}
