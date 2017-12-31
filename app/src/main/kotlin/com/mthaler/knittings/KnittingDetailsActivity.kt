package com.mthaler.knittings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.FileProvider
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.startActivity
import java.io.File
import java.io.FileNotFoundException

/**
 * Activity that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsActivity : AppCompatActivity(), AnkoLogger {

    private var knittingID: Long = -1
    private var currentPhotoPath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting_details)

        val toolbar = find<Toolbar>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(KnittingDetailsActivity.EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            // initialize the knitting details fragment with the knitting it should display
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_knitting_details) as KnittingDetailsFragment
            if (fragment != null) {
                val knitting = datasource.getKnitting(id)
                fragment.init(knitting)
            } else {
                error("Could not get knitting details fragment")
            }

            // start edit knitting details fragment if the user clicks the floating action button
            val fab = findViewById<FloatingActionButton>(R.id.edit_knitting_details)
            fab.setOnClickListener {
                startActivity< EditKnittingDetailsActivity>(EditKnittingDetailsActivity.EXTRA_KNITTING_ID to id)
            }
            knittingID = id
        } else {
            error("Could not get knitting id")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knitting_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_show_gallery -> {
                startActivity<PhotoGalleryActivity>()
                return true
            }
            R.id.menu_item_add_photo -> {
                // take photo icon clicked, ask user if photo should be taken or imported from gallery
                val b = AlertDialog.Builder(this)
                val layout = layoutInflater.inflate(R.layout.dialog_take_photo, null)
                val buttonTakePhoto = layout.find<Button>(R.id.button_take_photo)
                val buttonImportPhoto = layout.find<Button>(R.id.buttom_import_photo)
                b.setView(layout)
                b.setNegativeButton(R.string.dialog_button_cancel) { diag, i -> diag.dismiss()}
                val d = b.create()
                buttonTakePhoto.setOnClickListener {
                    d.dismiss()
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val knitting = datasource.getKnitting(knittingID)
                    currentPhotoPath = datasource.getPhotoFile(knitting)
                    debug("Set current photo path: " + currentPhotoPath)
                    val packageManager = packageManager
                    val canTakePhoto = currentPhotoPath != null && takePictureIntent.resolveActivity(packageManager) != null
                    if (canTakePhoto) {
                        val uri = FileProvider.getUriForFile(this, "com.mthaler.knittings.fileprovider", currentPhotoPath!!)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        debug("Created take picture intent")
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
                buttonImportPhoto.setOnClickListener {
                    d.dismiss()
                    val knitting = datasource.getKnitting(knittingID)
                    currentPhotoPath = datasource.getPhotoFile(knitting)
                    debug("Set current photo path: " + currentPhotoPath)
                    val photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    startActivityForResult(photoPickerIntent, REQUEST_IMAGE_IMPORT)
                }
                d.show()
                return true
            }
            R.id.menu_item_delete_knitting -> {
                // show alert asking user to confirm that knitting should be deleted
                alert {
                    title = resources.getString(R.string.delete_knitting_dialog_title)
                    message = resources.getString(R.string.delete_knitting_dialog_question)
                    positiveButton(resources.getString(R.string.delete_knitting_dialog_delete_button)) {
                        val knitting = datasource.getKnitting(knittingID)
                        // delete all photos from the database
                        datasource.deleteAllPhotos(knitting)
                        // delete database entry
                        datasource.deleteKnitting(knitting)
                        finish()
                    }
                    negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
                }.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // add photo to database
            debug("Received result for take photo intent")
            val orientation = PictureUtils.getOrientation(currentPhotoPath!!.absolutePath)
            val preview = PictureUtils.decodeSampledBitmapFromPath(currentPhotoPath!!.absolutePath, 200, 200)
            val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
            val photo = datasource.createPhoto(currentPhotoPath!!,knittingID, rotatedPreview, "")
            debug("Created new photo from " + currentPhotoPath + ", knitting id " + knittingID)
            // add first photo as default photo
            val knitting = datasource.getKnitting(knittingID)
            if (knitting.defaultPhoto == null) {
                debug("Set $photo as default photo")
                datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
            }
        } else if (requestCode == REQUEST_IMAGE_IMPORT) {
            try {
                debug("Received result for import photo intent")
                val imageUri = data!!.data
                PictureUtils.copy(imageUri, currentPhotoPath!!, this)
                val orientation = PictureUtils.getOrientation(currentPhotoPath!!.absolutePath)
                val preview = PictureUtils.decodeSampledBitmapFromPath(currentPhotoPath!!.absolutePath, 200, 200)
                val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
                val photo = datasource.createPhoto(currentPhotoPath!!, knittingID, rotatedPreview, "")
                debug("Created new photo from " + currentPhotoPath + ", knitting id " + knittingID)
                // add first photo as default photo
                val knitting = datasource.getKnitting(knittingID)
                if (knitting!!.defaultPhoto == null) {
                    debug("Set $photo as default photo")
                    datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }


        }
    }

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
        private val REQUEST_IMAGE_CAPTURE = 0
        private val REQUEST_IMAGE_IMPORT = 1
    }
}
