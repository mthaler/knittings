package com.mthaler.knittings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import org.jetbrains.anko.*
import com.mthaler.knittings.database.datasource
import java.io.File
import java.io.FileNotFoundException

interface CanTakePhoto : AnkoLogger {

    var currentPhotoPath: File?

    fun startActivityForResult(intent: Intent, requestCode: Int)

    fun takePhoto(context: Context, layoutInflater: LayoutInflater, knittingID: Long): Boolean {
        // take photo icon clicked, ask user if photo should be taken or imported from gallery
        val b = AlertDialog.Builder(context)
        val layout = layoutInflater.inflate(R.layout.dialog_take_photo, null)
        val buttonTakePhoto = layout.find<Button>(R.id.button_take_photo)
        val buttonImportPhoto = layout.find<Button>(R.id.buttom_import_photo)
        b.setView(layout)
        val d = b.create()
        buttonTakePhoto.setOnClickListener {
            d.dismiss()
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val knitting = context.datasource.getKnitting(knittingID)
            currentPhotoPath = context.datasource.getPhotoFile(knitting)
            debug("Set current photo path: " + currentPhotoPath)
            val packageManager = context.packageManager
            val canTakePhoto = currentPhotoPath != null && takePictureIntent.resolveActivity(packageManager) != null
            if (canTakePhoto) {
                val uri = FileProvider.getUriForFile(context, "com.mthaler.knittings.fileprovider", currentPhotoPath!!)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                debug("Created take picture intent")
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
        buttonImportPhoto.setOnClickListener {
            d.dismiss()
            val knitting = context.datasource.getKnitting(knittingID)
            currentPhotoPath = context.datasource.getPhotoFile(knitting)
            debug("Set current photo path: " + currentPhotoPath)
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, REQUEST_IMAGE_IMPORT)
        }
        d.show()
        return true
    }

    fun onActivityResult(context: Context, knittingID: Long, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // add photo to database
            debug("Received result for take photo intent")
            val orientation = PictureUtils.getOrientation(currentPhotoPath!!.absolutePath)
            val preview = PictureUtils.decodeSampledBitmapFromPath(currentPhotoPath!!.absolutePath, 200, 200)
            val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
            val photo = context.datasource.createPhoto(currentPhotoPath!!,knittingID, rotatedPreview, "")
            debug("Created new photo from $currentPhotoPath, knitting id $knittingID")
            // add first photo as default photo
            val knitting = context.datasource.getKnitting(knittingID)
            if (knitting.defaultPhoto == null) {
                debug("Set $photo as default photo")
                context.datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
            }
        } else if (requestCode == REQUEST_IMAGE_IMPORT) {
            try {
                debug("Received result for import photo intent")
                val imageUri = data!!.data
                PictureUtils.copy(imageUri, currentPhotoPath!!, context)
                val orientation = PictureUtils.getOrientation(currentPhotoPath!!.absolutePath)
                val preview = PictureUtils.decodeSampledBitmapFromPath(currentPhotoPath!!.absolutePath, 200, 200)
                val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
                val photo = context.datasource.createPhoto(currentPhotoPath!!, knittingID, rotatedPreview, "")
                debug("Created new photo from $currentPhotoPath, knitting id $knittingID")
                // add first photo as default photo
                val knitting = context.datasource.getKnitting(knittingID)
                if (knitting.defaultPhoto == null) {
                    debug("Set $photo as default photo")
                    context.datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        val REQUEST_IMAGE_CAPTURE = 0
        val REQUEST_IMAGE_IMPORT = 1
    }
}