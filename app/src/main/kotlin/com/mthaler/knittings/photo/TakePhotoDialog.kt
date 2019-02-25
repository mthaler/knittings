package com.mthaler.knittings.photo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.widget.Button
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.find
import java.io.File
import android.content.ClipData
import android.net.Uri
import android.os.Build



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
            d.dismiss()
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val knitting = context.datasource.getKnitting(knittingID)
            // create a photo file for the photo
            val f = context.datasource.getPhotoFile(knitting)
            val packageManager = context.packageManager
            val canTakePhoto = f != null && takePictureIntent.resolveActivity(packageManager) != null
            if (canTakePhoto) {
                val uri = if (Build.VERSION.SDK_INT == 15) {
                    Uri.fromFile(f)
                } else {
                    FileProvider.getUriForFile(context, "com.mthaler.knittings.fileprovider", f!!)
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <= 21) {
                    takePictureIntent.clipData = ClipData.newRawUri("", uri)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                debug("Created take picture intent")
                takePhoto(f!!, takePictureIntent)
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

    /**
     * Should be called from onActivityResult when the image capture activity returns
     *
     * @param context Context
     * @param knittingID ID of the knitting for which a photo should be added
     * @param file photo file
     */
    fun handleTakePhotoResult(context: Context,
                              knittingID: Long,
                              file: File) {
        // add photo to database
        val orientation = PictureUtils.getOrientation(file.absolutePath)
        val preview = PictureUtils.decodeSampledBitmapFromPath(file.absolutePath, 200, 200)
        val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
        val photo = context.datasource.addPhoto(Photo(-1, file, knittingID, "", rotatedPreview))
        debug("Created new photo from $file, knitting id $knittingID")
        // add first photo as default photo
        val knitting = context.datasource.getKnitting(knittingID)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val useNewestAsPreview = prefs.getBoolean(context.resources.getString(R.string.key_photos_use_newest_as_preview), true)
        if (useNewestAsPreview) {
            context.datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
        } else {
            if (knitting.defaultPhoto == null) {
                debug("Set $photo as default photo")
                context.datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
            }
        }
    }

    /**
     * Should be called from onActivityResult when the import image activity returns
     *
     * @param context Context
     * @param knittingID ID of the knitting for which a photo should be added
     * @param file photo file
     * @param data data returned from import image activity
     */
    fun handleImageImportResult(context: Context,
                                knittingID: Long,
                                file: File,
                                data: Intent) {
        val imageUri = data.data
        PictureUtils.copy(imageUri, file, context)
        val orientation = PictureUtils.getOrientation(file.absolutePath)
        val preview = PictureUtils.decodeSampledBitmapFromPath(file.absolutePath, 200, 200)
        val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
        val photo = context.datasource.addPhoto(Photo(-1, file, knittingID, "", rotatedPreview))
        debug("Created new photo from $file, knitting id $knittingID")
        // add first photo as default photo
        val knitting = context.datasource.getKnitting(knittingID)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val useNewestAsPreview = prefs.getBoolean(context.resources.getString(R.string.key_photos_use_newest_as_preview), true)
        if (useNewestAsPreview) {
            context.datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
        } else {
            if (knitting.defaultPhoto == null) {
                debug("Set $photo as default photo")
                context.datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
            }
        }
    }
}