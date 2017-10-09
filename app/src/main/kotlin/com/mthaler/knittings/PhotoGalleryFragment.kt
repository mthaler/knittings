package com.mthaler.knittings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.GridView
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.*
import android.widget.Toast
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.io.*

/**
 * A fragment that displays a list of photos using a grid
 */
class PhotoGalleryFragment : Fragment(), AnkoLogger {

    private var knitting: Knitting? = null

    private lateinit var gridView: GridView
    private var currentPhotoPath: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH)!!)
                debug("Set current photo path: " + currentPhotoPath)
            }
            if (savedInstanceState.containsKey(KNITTING_ID)) {
                knitting = KnittingsDataSource.getInstance(activity).getKnitting(savedInstanceState.getLong(KNITTING_ID))
                debug("Set knitting: " + knitting)
            }
        } else {
            val knittingID = arguments.getLong(KNITTING_ID)
            knitting = KnittingsDataSource.getInstance(activity).getKnitting(knittingID)
            debug("Set knitting: " + knitting)
        }

        gridView = v.findViewById(R.id.gridView)
        val photos = KnittingsDataSource.getInstance(activity).getAllPhotos(knitting!!)
        val gridAdapter = GridViewAdapter(activity, R.layout.grid_item_layout, photos)
        gridView.adapter = gridAdapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            if (position < gridView.adapter.count - 1) {
                // photo clicked, show photo in photo activity
                val photo = parent.getItemAtPosition(position) as Photo
                startActivity<PhotoActivity>(PhotoActivity.EXTRA_PHOTO_ID to photo.id, KnittingActivity.EXTRA_KNITTING_ID to knitting!!.id)
            } else {
                // take photo icon clicked, ask user if photo should be taken or imported from gallery
                val b = AlertDialog.Builder(activity)
                val layout = inflater.inflate(R.layout.dialog_take_photo, parent, false)
                val buttonTakePhoto = layout.find<Button>(R.id.button_take_photo)
                val buttonImportPhoto = layout.find<Button>(R.id.buttom_import_photo)
                b.setView(layout)
                b.setNegativeButton(R.string.dialog_button_cancel) { diag, i -> diag.dismiss()}
                val d = b.create()
                buttonTakePhoto.setOnClickListener {
                    d.dismiss()
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    currentPhotoPath = KnittingsDataSource.getInstance(activity).getPhotoFile(knitting!!)
                    debug("Set current photo path: " + currentPhotoPath)
                    val packageManager = activity.packageManager
                    val canTakePhoto = currentPhotoPath != null && takePictureIntent.resolveActivity(packageManager) != null
                    if (canTakePhoto) {
                        val uri = FileProvider.getUriForFile(context, "com.mthaler.knittings.fileprovider", currentPhotoPath)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        debug("Created take picture intent")
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
                buttonImportPhoto.setOnClickListener {
                    d.dismiss()
                    currentPhotoPath = KnittingsDataSource.getInstance(activity).getPhotoFile(knitting!!)
                    debug("Set current photo path: " + currentPhotoPath)
                    val photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    startActivityForResult(photoPickerIntent, REQUEST_IMAGE_IMPORT)
                }
                d.show()
            }
        }

        return v
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
            val photo = KnittingsDataSource.getInstance(activity).createPhoto(currentPhotoPath!!, knitting!!.id, rotatedPreview, "")
            debug("Created new photo from " + currentPhotoPath + ", knitting id " + knitting?.id)
            // add first photo as default photo
            if (knitting!!.defaultPhoto == null) {
                debug("Set $photo as default photo")
                knitting = knitting?.copy(defaultPhoto = photo)
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }
            // update grid view
            val photos = KnittingsDataSource.getInstance(activity).getAllPhotos(knitting!!)
            val gridAdapter = GridViewAdapter(activity, R.layout.grid_item_layout, photos)
            gridView.adapter = gridAdapter
        } else if (requestCode == REQUEST_IMAGE_IMPORT) {
            try {
                debug("Received result for import photo intent")
                val imageUri = data!!.data
                PictureUtils.copy(imageUri, currentPhotoPath!!, activity)
                val orientation = PictureUtils.getOrientation(currentPhotoPath!!.absolutePath)
                val preview = PictureUtils.decodeSampledBitmapFromPath(currentPhotoPath!!.absolutePath, 200, 200)
                val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
                val photo = KnittingsDataSource.getInstance(activity).createPhoto(currentPhotoPath!!, knitting!!.id, rotatedPreview, "")
                debug("Created new photo from " + currentPhotoPath + ", knitting id " + knitting?.id)
                // add first photo as default photo
                if (knitting!!.defaultPhoto == null) {
                    debug("Set $photo as default photo")
                    knitting = knitting?.copy(defaultPhoto = photo)
                    KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
                }
                // update grid view
                val photos = KnittingsDataSource.getInstance(activity).getAllPhotos(knitting!!)
                val gridAdapter = GridViewAdapter(activity, R.layout.grid_item_layout, photos)
                gridView.adapter = gridAdapter
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_LONG).show()
            }


        }
    }

    override fun onResume() {
        super.onResume()
        if (knitting != null) {
            val photos = KnittingsDataSource.getInstance(activity).getAllPhotos(knitting!!)
            val gridAdapter = GridViewAdapter(activity, R.layout.grid_item_layout, photos)
            gridView.adapter = gridAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (currentPhotoPath != null) {
            outState!!.putString(CURRENT_PHOTO_PATH, currentPhotoPath!!.absolutePath)
        }
        if (knitting != null) {
            outState!!.putLong(KNITTING_ID, knitting!!.id)
        }
        super.onSaveInstanceState(outState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (knitting != null) {
            if (isVisibleToUser) {
                // the fragment became visible because the user selected it in the view pager
                // get current knitting from database
                knitting = KnittingsDataSource.getInstance(activity).getKnitting(knitting!!.id)
            } else {
                // the fragment became invisible because the user selected another tab in the view pager
                // save current knitting to database
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }
        }
    }

    companion object : AnkoLogger {

        private val CURRENT_PHOTO_PATH = "current_photo_path"
        private val KNITTING_ID = "knitting_id"
        private val REQUEST_IMAGE_CAPTURE = 0
        private val REQUEST_IMAGE_IMPORT = 1

        fun newInstance(knitting: Knitting): PhotoGalleryFragment {
            val fragment = PhotoGalleryFragment()
            val args = Bundle()
            args.putLong(KNITTING_ID, knitting.id)
            fragment.arguments = args
            debug("Created new PhotoGalleryFragment with knitting id: " + knitting.id)
            return fragment
        }
    }
}
