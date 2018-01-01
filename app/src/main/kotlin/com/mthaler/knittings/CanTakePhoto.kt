package com.mthaler.knittings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.widget.Button
import org.jetbrains.anko.*
import com.mthaler.knittings.database.datasource
import java.io.File

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
        b.setNegativeButton(R.string.dialog_button_cancel) { diag, i -> diag.dismiss()}
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
                startActivityForResult(takePictureIntent, KnittingDetailsActivity.REQUEST_IMAGE_CAPTURE)
            }
        }
        buttonImportPhoto.setOnClickListener {
            d.dismiss()
            val knitting = context.datasource.getKnitting(knittingID)
            currentPhotoPath = context.datasource.getPhotoFile(knitting)
            debug("Set current photo path: " + currentPhotoPath)
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, KnittingDetailsActivity.REQUEST_IMAGE_IMPORT)
        }
        d.show()
        return true
    }
}