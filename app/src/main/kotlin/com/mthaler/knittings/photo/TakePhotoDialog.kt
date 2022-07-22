package com.mthaler.knittings.photo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.database.PhotoDataSource
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import com.mthaler.knittings.utils.copy
import com.mthaler.knittings.utils.getOrientation
import java.io.File

object TakePhotoDialog {

    private const val TAG = "TakePhotoDialog"

    /**
     * Creates the take photo dialog used to ask the user if he wants to take a photo or import a photo
     *
     * @param context Context
     * @param layoutInflater layout inflater
     * @param takePhoto function that is called if the user wants to take a photo
     * @param importPhoto function that is called if the user wants to import a photo
     */
    fun create(
        context: Context,
        layoutInflater: LayoutInflater,
        takePhoto: (Uri, Intent) -> Unit,
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
            // create a photo file for the photo
            val uri =
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    context.applicationContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues());
                }
                else
                {
                    context.applicationContext.contentResolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, ContentValues());
                }
            uri?.let {
                val packageManager = context.packageManager
                val takePictureIntent = dispatchTakePictureIntent(context, packageManager, authority, it)
                val canTakePhoto = takePictureIntent.resolveActivity(packageManager) != null
                if (canTakePhoto) {
                    Log.d(TAG, "Created take picture intent")
                    takePhoto(it, takePictureIntent)
                }
            }
        }
        buttonImportPhoto.setOnClickListener {
            d.dismiss()
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
     * @param ownerID id of the owner of the photo
     * @param file photo file
     */
    suspend fun handleTakePhotoResult(context: Context, ownerID: Long, uri: Uri) {
        // add photo to database
        val compressed = PictureUtils.compress(context, file)
        if (compressed.length() < file.length()) {
            if (!file.delete()) {
                error("Could not delete $file")
            }
            compressed.copy(file)
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        } else {
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        }
        val orientation = file.toUri().getOrientation(context)
        val preview = PictureUtils.decodeSampledBitmapFromPath(file.absolutePath, 200, 200)
        val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
        val ds = KnittingsDataSource as PhotoDataSource
        val photo = ds.addPhoto(Photo(-1, file, ownerID, "", rotatedPreview))
        Log.d(TAG, "Created new photo from $file, owner id $ownerID")
        // add first photo as default photo
        Log.w(TAG, "set default photo: $ownerID")
        ds.setDefaultPhoto(ownerID, photo)
    }

    /**
     * Should be called from onActivityResult when the import image activity returns
     *
     * @param context Context
     * @param ownerID id of the owner of the photo
     * @param file photo file
     * @param data data returned from import image activity
     */
    suspend fun handleImageImportResult(context: Context, ownerID: Long, file: File, data: Intent) {
        val imageUri = data.data
        PictureUtils.copy(imageUri!!, file, context)
        val compressed = PictureUtils.compress(context, file)
        if (compressed.length() < file.length()) {
            if (!file.delete()) {
                error("Could not delete $file")
            }
            compressed.copy(file)
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        } else {
            if (!compressed.delete()) {
                error("Could not delete $compressed")
            }
        }
        val orientation = file.toUri().getOrientation(context)
        val preview = PictureUtils.decodeSampledBitmapFromPath(file.absolutePath, 200, 200)
        val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
        val ds = KnittingsDataSource as PhotoDataSource
        val photo = ds.addPhoto(Photo(-1, file, ownerID, "", rotatedPreview))
        Log.d(TAG, "Created new photo from $file, owner id $ownerID")
        // add first photo as default photo
        ds.setDefaultPhoto(ownerID, photo)
    }

    private fun getPhotoFile(context: Context): File? {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return if (externalFilesDir != null) File(externalFilesDir, Photo.photoFilename) else null
    }

    private fun dispatchTakePictureIntent(context: Context, packageManager: PackageManager, authority: String, f: File): Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        // Ensure that there's a camera activity to handle the intent
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            // Continue only if the File was successfully created
            f.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    authority,
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
        }
    }

}