package com.mthaler.knittings.photo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.widget.Button
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.find
import java.io.File

object TakePhotoDialog : AnkoLogger {

    /**
     * Creates the take photo dialog used to ask the user if he wants to take a photo or import a photo
     *
     * @param context Context
     * @param layoutInflater layout inflater
     * @param knittingID ID of the knitting for which a photo should be added
     * @param takePhoto function that is called if the user wants to take a photo
     * @param importPhoto function that is called if the user wants to import a photo
     */
    fun create(context: Context,
               layoutInflater: LayoutInflater,
               knittingID: Long,
               takePhoto: (File, Intent) -> Unit,
               importPhoto: (File, Intent) -> Unit): AlertDialog {
        // create the dialog
        val b = AlertDialog.Builder(context)
        @SuppressLint("InflateParams")
        val layout = layoutInflater.inflate(R.layout.dialog_take_photo, null)
        val buttonTakePhoto = layout.find<Button>(R.id.button_take_photo)
        val buttonImportPhoto = layout.find<Button>(R.id.buttom_import_photo)
        b.setView(layout)
        val d = b.create()
        // take a photo if the user clicks the take photo button
        buttonTakePhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val knitting = context.datasource.getKnitting(knittingID)
            // create a photo file for the photo
            val f = context.datasource.getPhotoFile(knitting)
            val packageManager = context.packageManager
            val canTakePhoto = f != null && takePictureIntent.resolveActivity(packageManager) != null
            if (canTakePhoto) {
                val uri = FileProvider.getUriForFile(context, "com.mthaler.knittings.fileprovider", f!!)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                debug("Created take picture intent")
                takePhoto(f, takePictureIntent)
            }
        }
        buttonImportPhoto.setOnClickListener {
            d.dismiss()
            val knitting = context.datasource.getKnitting(knittingID)
            val f = context.datasource.getPhotoFile(knitting)
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            importPhoto(f!!, photoPickerIntent)
        }
        return d
    }
}