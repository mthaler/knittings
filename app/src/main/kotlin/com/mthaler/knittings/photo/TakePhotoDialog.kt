package com.mthaler.knittings.photo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.content.FileProvider
import com.mthaler.dbapp.database.PhotoRepository
import com.mthaler.dbapp.model.Photo
import com.mthaler.dbapp.utils.FileUtils
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Knitting
import java.io.File

object TakePhotoDialog {

    private const val TAG = "TakePhotoDialog"

    /**
     * Creates the take photo dialog used to ask the user if he wants to take a photo or import a photo
     *
     * @param context Context
     * @param layoutInflater layout inflater
     * @param knittingID ID of the knitting for which a photo should be added
     * @param takePhoto function that is called if the user wants to take a photo
     * @param importPhoto function that is called if the user wants to import a photo
     */
    fun create(
        context: Context,
        layoutInflater: LayoutInflater,
        knittingID: Long,
        takePhoto: (File, Intent) -> Unit,
        importPhoto: (File, Intent) -> Unit
    ): AlertDialog {

        // create the dialog
        val b = AlertDialog.Builder(context)
        @SuppressLint("InflateParams")
        val layout = layoutInflater.inflate(R.layout.dialog_take_photo, null)
        val buttonTakePhoto = layout.findViewById<Button>(R.id.button_take_photo)
        val buttonImportPhoto = layout.findViewById<Button>(R.id.buttom_import_photo)
        b.setView(layout)
        val d = b.create()
        // take a photo if the user clicks the take photo button
        buttonTakePhoto.setOnClickListener {
            d.dismiss()
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val knitting = KnittingsDataSource.getKnitting(knittingID)
            // create a photo file for the photo
            val f = getPhotoFile(context)?.let {
                val packageManager = context.packageManager
                val canTakePhoto = takePictureIntent.resolveActivity(packageManager) != null
                if (canTakePhoto) {
                    val uri = FileProvider.getUriForFile(context, "com.mthaler.knittings.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    // this is needed on older android versions to net get a security exception
                    if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <= 21) {
                        takePictureIntent.clipData = ClipData.newRawUri("", uri)
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    Log.d(TAG, "Created take picture intent")
                    takePhoto(it, takePictureIntent)
                }
            }
        }
        buttonImportPhoto.setOnClickListener {
            d.dismiss()
            val knitting = KnittingsDataSource.getKnitting(knittingID)
            getPhotoFile(context)?.let {
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                importPhoto(it, photoPickerIntent)
            }
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
    suspend fun handleTakePhotoResult(context: Context, knittingID: Long, file: File) {
        // add photo to database
        val compressed = PictureUtils.compress(context, file)
        if (compressed.length() < file.length()) {
            if (!file.delete()) {
                error("Could not delete $file")
            }
            FileUtils.copy(compressed, file)
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        } else {
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        }
        val orientation = PictureUtils.getOrientation(file.absolutePath)
        val preview = PictureUtils.decodeSampledBitmapFromPath(file.absolutePath, 200, 200)
        val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
        val photo = PhotoRepository.addPhoto(Photo(-1, file, knittingID, "", rotatedPreview))
        Log.d(TAG, "Created new photo from $file, knitting id $knittingID")
        // add first photo as default photo
        val knitting = KnittingsDataSource.getKnitting(knittingID)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val useNewestAsPreview = prefs.getBoolean(context.resources.getString(R.string.key_photos_use_newest_as_preview), true)
        if (useNewestAsPreview) {
            KnittingsDataSource.updateKnitting(knitting.copy(defaultPhoto = photo))
        } else {
            if (knitting.defaultPhoto == null) {
                Log.d(TAG, "Set $photo as default photo")
                KnittingsDataSource.updateKnitting(knitting.copy(defaultPhoto = photo))
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
    suspend fun handleImageImportResult(context: Context, knittingID: Long, file: File, data: Intent) {
        val imageUri = data.data
        PictureUtils.copy(imageUri!!, file, context)
        val compressed = PictureUtils.compress(context, file)
        if (compressed.length() < file.length()) {
            if (!file.delete()) {
                error("Could not delete $file")
            }
            FileUtils.copy(compressed, file)
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        } else {
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        }
        val orientation = PictureUtils.getOrientation(file.absolutePath)
        val preview = PictureUtils.decodeSampledBitmapFromPath(file.absolutePath, 200, 200)
        val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
        val photo = PhotoRepository.addPhoto(Photo(-1, file, knittingID, "", rotatedPreview))
        Log.d(TAG, "Created new photo from $file, knitting id $knittingID")
        // add first photo as default photo
        val knitting = KnittingsDataSource.getKnitting(knittingID)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val useNewestAsPreview = prefs.getBoolean(context.resources.getString(R.string.key_photos_use_newest_as_preview), true)
        if (useNewestAsPreview) {
            KnittingsDataSource.updateKnitting(knitting.copy(defaultPhoto = photo))
        } else {
            if (knitting.defaultPhoto == null) {
                Log.d(TAG, "Set $photo as default photo")
                KnittingsDataSource.updateKnitting(knitting.copy(defaultPhoto = photo))
            }
        }
    }

    fun getPhotoFile(context: Context): File? {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return if (externalFilesDir != null) File(externalFilesDir, Photo.photoFilename) else null
    }
}